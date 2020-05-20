from subprocess import Popen, PIPE, STDOUT
from os import path
import json
from typing import List, Tuple, Optional
import tempfile
import pandas as pd


class Admissable:
    def __init__(self, exposure: str, outcome: str, admissable: List[str]):
        self.exposure = exposure
        self.outcome = outcome
        self.admissable = admissable

    def __eq__(self, other) -> bool:
        if not isinstance(other, self.__class__):
            return False
        return hash(self) == hash(other)

    def __hash__(self) -> int:
        return hash((self.exposure, self.outcome, tuple(sorted(map(lambda x: tuple(sorted(x)), self.admissable)))))


class DirectIdentification:
    def __init__(self, exposure: str, outcome: str, admissables: List[List[str]]):
        self.exposure = exposure
        self.outcome = outcome
        self.admissables = admissables

    def identified(self) -> bool:
        return len(self.admissables) != 0

    def __eq__(self, other) -> bool:
        if not isinstance(other, self.__class__):
            return False
        return hash(self) == hash(other)

    def __hash__(self) -> int:
        return hash(
            (self.exposure, self.outcome, tuple(sorted(map(lambda x: tuple(sorted(x)), self.admissables)))))

    def select(self, index: int) -> Admissable:
        if index < 0 or index >= len(self.admissables):
            raise OverflowError(f"{index} out of bound")
        return Admissable(self.exposure, self.outcome, self.admissables[index])


def identification_parse(content: str) -> List[DirectIdentification]:
    identification: List[List[str, List[str]]] = json.loads(content, encoding="utf-8")
    res = []
    for i in identification:
        link = i[0].split('->')
        v1 = link[0]
        v2 = link[1]
        admissibles = list(map(lambda x: list(filter(lambda a: a != "", x.split(','))), i[1]))
        res.append(DirectIdentification(v1, v2, admissibles))
    return res


def identify(dot_str: str, paths: Optional[List[str]] = None) -> List[DirectIdentification]:
    with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8") as tf:
        tf.write(dot_str)
        tf.flush()
        dot_path = tf.name
        arguments = ["node", path.join(path.dirname(__file__), "lib", "run.js"), "--dot", dot_path, "--mode",
                     "identify"]
        if paths is not None:
            arguments.append("--path")
            arguments.append(f"'${','.join(list(paths))}'")
        with Popen(arguments, stdout=PIPE, stderr=STDOUT) as process:
            process.wait()
            if process.returncode != 0:
                raise RuntimeError(process.stderr.read().decode("utf-8"))
            s = process.stdout.read().decode("utf-8")
    return identification_parse(s)


def estimate_atomic_effect(estimator, admissable: Admissable, data: pd.DataFrame,
                           *args, **kwargs):
    exposure = admissable.exposure
    outcome = admissable.outcome
    ad = admissable.admissable
    estimator.fit(data[[exposure, *ad]], data[outcome], *args, **kwargs)
    return estimator


def estimate(estimators: List, admissables: List[Admissable], data: List[pd.DataFrame], *args, **kwargs) -> List:
    res = []
    for (i, e) in enumerate(estimators):
        d = data[i]
        ad = admissables[i]
        res.append(estimate_atomic_effect(e, ad, d, *args, *kwargs))
    return res


def use_atomic_effect(estimator, admissable: Admissable, data: pd.DataFrame, *args, **kwargs
                      ) -> pd.Series:
    return estimator.predict(data[[admissable.exposure, *admissable.admissable]], *args, **kwargs)


def use(estimators: List, admissables: List[Admissable], test: pd.DataFrame,
        *args, **kwargs) -> pd.DataFrame:
    mapping = {}
    for f in test.columns.to_list():
        mapping[f] = test[f]
    for (i, e) in enumerate(estimators):
        ad = admissables[i]
        curr_test = {}
        for field in [ad.exposure, *ad.admissable]:
            curr_test[field] = mapping[field]
        mapping[ad.outcome] = use_atomic_effect(e, ad, pd.DataFrame(curr_test), *args, **kwargs)
    return pd.DataFrame(mapping)
