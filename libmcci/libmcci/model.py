from typing import List, Optional, Iterable, Tuple
import pygraphviz as g
import pandas as pd

from libmcci.dowhy import Admissable, DirectIdentification, identify, \
    estimate_atomic_effect, estimate, use_atomic_effect, use
from libmcci.exceptions import CausalModelNotDefinedError
from libmcci.instrument import find_instruments, estimate_instrument, use_instrument


class CausalModel:
    EXPOSURE = "exposure"
    OUTCOME = "outcome"
    LATENT = "latent"

    def __init__(self):
        self.graph = g.AGraph.from_string("digraph {}")

    @property
    def dots(self) -> str:
        return self.graph.to_string()

    @dots.setter
    def dots(self, ds: str):
        self.graph = g.AGraph.from_string(ds)

    @property
    def exposure(self) -> Optional[str]:
        for n in self.graph.nodes():
            if self.EXPOSURE in n.attr:
                return n.name
        return None

    @exposure.setter
    def exposure(self, exp: str):
        attr_set = False
        for n in self.graph.nodes():
            if n.name == exp:
                n.attr[self.EXPOSURE] = ""
            if self.EXPOSURE in n.attr:
                del n.attr[self.EXPOSURE]
        if not attr_set:
            n = g.Node(self.graph, exp)
            n.attr[self.EXPOSURE] = ""
            self.graph.add_node(n)

    @property
    def outcome(self) -> Optional[str]:
        for n in self.graph.nodes():
            if self.OUTCOME in n.attr:
                return n.name
        return None

    @outcome.setter
    def outcome(self, out: str):
        attr_set = False
        for n in self.graph.nodes():
            if n.name == out:
                n.attr[self.OUTCOME] = ""
            if self.OUTCOME in n.attr:
                del n.attr[self.OUTCOME]
        if not attr_set:
            n = g.Node(self.graph, name=out)
            n.attr[self.OUTCOME] = ""
            self.graph.add_node(n)

    @property
    def latents(self) -> List[str]:
        res = []
        for n in self.graph.nodes():
            if self.LATENT in n.attr:
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
                n.attr[self.LATENT] = ""
                ls.remove(n.name)
        for l in ls:
            n = g.Node(self.graph, name=l)
            n.attr[self.LATENT] = ""
            self.graph.add_node(n)

    def remove_latents(self, *ls: Iterable[str]):
        ls = set(ls)
        for n in self.graph.nodes():
            if (n.name in ls) and (self.LATENT in n.attr):
                del n.attr[self.LATENT]

    def clear_latents(self):
        for n in self.graph.nodes():
            if self.LATENT in n.attr:
                del n.attr[self.LATENT]

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
