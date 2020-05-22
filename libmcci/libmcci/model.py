from typing import List, Optional, Iterable, Tuple, Union, Dict
import pandas as pd
from os import path
import os
from libmcci.dowhy import Admissable, DirectIdentification, identify, \
    estimate_atomic_effect, estimate, use_atomic_effect, use
from libmcci.exceptions import CausalModelNotDefinedError
from libmcci.instrument import find_instruments, estimate_instrument, use_instrument
import re
import pygraphviz as g

from libmcci.mediation import group_by_count

try:
    from IPython import display

    jupyter_env = True
except Exception as e:
    jupyter_env = False


def node_get_attr(n: g.Node, attr: str) -> Optional[str]:
    a = n.attr[attr]
    if a == "" or a is None:
        return None
    return a


def node_has_attr(n: g.Node, attr: str) -> bool:
    return node_get_attr(n, attr) is not None


def node_del_attr(n: g.Node, attr: str):
    del n.attr[attr]


def node_set_attr(n: g.Node, attr: str, value: str):
    n.attr[attr] = value


class CausalModel:
    EXPOSURE = "exposure"
    OUTCOME = "outcome"
    LATENT = "latent"
    BUG_AVOID = " "

    def __init__(self):
        self.graph = g.AGraph()
        self.data: List[pd.DataFrame] = []
        self.data_mapping: Dict[str, pd.DataFrame] = {}

    @property
    def dots(self) -> str:
        return self.graph.to_string()

    @dots.setter
    def dots(self, ds: str):
        ds = re.sub(r"(\s*[\"']?[^\"']+[\"\']?\s*)=\s*\"\"\s*", f"\\1=\"{self.BUG_AVOID}\"", ds)
        self.graph = g.AGraph(string=ds)

    @property
    def exposure(self) -> Optional[str]:
        for n in self.graph.nodes():
            if node_has_attr(n, self.EXPOSURE):
                return n.name
        return None

    @exposure.setter
    def exposure(self, exp: str):
        attr_set = False
        for n in self.graph.nodes():
            if n.name == exp:
                node_set_attr(n, self.EXPOSURE, self.BUG_AVOID)
            if node_has_attr(n, self.EXPOSURE):
                node_del_attr(n, self.EXPOSURE)
        if not attr_set:
            n = g.Node(self.graph, exp)
            node_set_attr(n, self.EXPOSURE, self.BUG_AVOID)
            self.graph.add_node(n)

    @property
    def outcome(self) -> Optional[str]:
        for n in self.graph.nodes():
            if node_has_attr(n, self.OUTCOME):
                return n.name
        return None

    @outcome.setter
    def outcome(self, out: str):
        attr_set = False
        for n in self.graph.nodes():
            if n.name == out:
                node_set_attr(n, self.OUTCOME, self.BUG_AVOID)
            if node_has_attr(n, self.OUTCOME):
                node_del_attr(n, self.OUTCOME)
        if not attr_set:
            n = g.Node(self.graph, name=out)
            node_set_attr(n, self.OUTCOME, self.BUG_AVOID)
            self.graph.add_node(n)

    @property
    def latents(self) -> List[str]:
        res = []
        for n in self.graph.nodes():
            if node_has_attr(n, self.LATENT):
                res.append(n.name)
        return res

    @latents.setter
    def latents(self, ls: Iterable[str]):
        self.clear_latents()
        self.add_latents(*ls)

    def add_latents(self, *ls: Iterable[str]):
        ls = set(ls)
        for n in self.graph.nodes():
            if n.name in ls:
                node_set_attr(n, self.LATENT, self.BUG_AVOID)
                ls.remove(n.name)
        for l in ls:
            n = g.Node(self.graph, name=l)
            node_set_attr(n, self.LATENT, self.BUG_AVOID)
            self.graph.add_node(n)

    def remove_latents(self, *ls: Iterable[str]):
        ls = set(ls)
        for n in self.graph.nodes():
            if (n.name in ls) and (node_has_attr(n, self.LATENT)):
                node_del_attr(n, self.LATENT)

    def clear_latents(self):
        for n in self.graph.nodes():
            if node_has_attr(n, self.LATENT):
                node_del_attr(n, self.LATENT)

    def identify(self) -> List[DirectIdentification]:
        if not self.defined():
            raise CausalModelNotDefinedError()
        return identify(self.graph.to_string())

    def find_instrument(self) -> List[List[str]]:
        if not self.defined():
            raise CausalModelNotDefinedError()
        return find_instruments(self.graph.to_string())

    def defined(self) -> bool:
        return (self.exposure is not None) or (self.outcome is not None)

    def estimate_atomic_effect(self, estimator, admissable: Admissable, data: pd.DataFrame,
                               *args, **kwargs):
        return estimate_atomic_effect(estimator, admissable, data, *args, **kwargs)

    def use_atomic_effect(self, estimator, admissable: Admissable, test: pd.DataFrame, *args,
                          **kwargs) -> pd.Series:
        return use_atomic_effect(estimator, admissable, test, *args, **kwargs)

    def estimate(self, estimators: List, admissables: List[Admissable], data: List[pd.DataFrame], *args,
                 **kwargs) -> List:
        return estimate(estimators, admissables, data, *args, **kwargs)

    def use(self, estimators: List, admissables: List[Admissable], test: pd.DataFrame,
            *args, **kwargs) -> pd.DataFrame:
        return use(estimators, admissables, test, *args, **kwargs)

    def estimate_instrument(self,
                            to_outcome_estimators: List,
                            to_outcome_admissables: List[Admissable],
                            to_outcome_data: List[pd.DataFrame],
                            to_exposure_estimators: List,
                            to_exposure_admissables: List[Admissable],
                            to_exposure_data: List[pd.DataFrame],
                            *args, **kwargs) -> Tuple[List, List]:
        return estimate_instrument(to_outcome_estimators, to_outcome_admissables, to_outcome_data,
                                   to_exposure_estimators, to_exposure_admissables, to_exposure_data, *args, **kwargs)

    def use_instrument(self,
                       to_outcome_estimators,
                       to_outcome_admissables: List[Admissable],
                       to_exposure_estimators,
                       to_exposure_admissables: List[Admissable],
                       test: pd.DataFrame,
                       estimator_fn,
                       *args, **kwargs) -> pd.DataFrame:
        return use_instrument(to_outcome_estimators, to_outcome_admissables, to_exposure_estimators,
                              to_exposure_admissables, test, estimator_fn, *args, **kwargs)

    @classmethod
    def load_graph(cls, basedir: str = "."):
        with open(path.join(basedir, "graph.dot"), "r", encoding="utf-8") as f:
            dots = f.read()
        new = cls()
        new.dots = dots
        return new

    def get_data(self, index: Union[int, str]) -> pd.DataFrame:
        if type(index) == int:
            return self.data[index]
        else:
            return self.data_mapping[index]

    def load_data(self, basedir: str = "."):
        files = sorted(os.listdir(basedir))
        self.data = []
        self.data_mapping = {}
        for f in files:
            fp = path.join(basedir, f)
            if f.startswith("data") and f.endswith(".csv") and path.isfile(fp):
                s = re.search(r"(?<=data).*(?=\.csv)", f)
                if s is None:
                    raise IOError(f"error in load file {fp}")
                else:
                    name = s.group()
                    d = pd.read_csv(fp, encoding="utf-8", header=None)
                    self.data.append(d)
                    self.data_mapping[name] = d

    def group_by_count(self, df: pd.DataFrame, exposure_col: str = "X", outcome_col: str = "Y",
                       mediation_col: str = "Z", count_col: str = "P") -> pd.DataFrame:
        return group_by_count(df, exposure_col, outcome_col, mediation_col, count_col)

    @classmethod
    def load_all(cls, basedir: str = "."):
        n = cls.load_graph(basedir)
        n.load_data(basedir)
        return n

    def plot(self, basedir="."):
        p = path.join(basedir, "graph.png")
        format = p.split(".")[-1]
        self.graph.draw(p, format=format, prog="dot")
        if jupyter_env:
            display.display(display.Image(filename=p, format=format))
        else:
            print(f"plot to file {p}")
