from typing import List, Optional, Iterable
import pygraphviz as g
import pandas as pd

from libmcci.dowhy import identify, estimate_direct_effect, Admissable, predict_direct_effect, DirectIdentification
from libmcci.exceptions import CausalModelNotDefinedError
from libmcci.instrument import find_instruments


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

    def estimate_direct_effect(self, estimator, data: pd.DataFrame, admissable: Admissable, *args, **kwargs):
        return estimate_direct_effect(estimator, data, admissable.exposure, admissable.outcome,
                                      admissable.admissable, *args, **kwargs)

    def predict_direct_effect(self, estimator, test: pd.DataFrame, admissable: Admissable, *args,
                              **kwargs) -> pd.Series:
        return predict_direct_effect(estimator, test, admissable.exposure, admissable.admissable, *args,
                                     **kwargs)

    def estimate(self, estimators: List, data: List[pd.DataFrame],
                 admissables: List[Admissable], *args, **kwargs) -> List:
        res = []
        for (i, e) in enumerate(estimators):
            d = data[i]
            ad = admissables[i]
            res.append(self.estimate_direct_effect(e, d, ad, *args, *kwargs))
        return res

    def predict(self, estimators: List, test: pd.DataFrame,
                admissables: List[Admissable], *args, **kwargs) -> pd.DataFrame:
        mapping = {}
        for f in test.columns.to_list():
            mapping[f] = test[f]
        for (i, e) in enumerate(estimators):
            ad = admissables[i]
            curr_test = {}
            for field in [ad.exposure, *ad.admissable]:
                curr_test[field] = mapping[field]
            mapping[ad.outcome] = self.predict_direct_effect(e, pd.DataFrame(curr_test), ad, *args, **kwargs)
        return pd.DataFrame(mapping)
