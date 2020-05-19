from subprocess import Popen, PIPE, STDOUT
from os import path
import json
from typing import List
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


def identify(dot_str: str) -> List[DirectIdentification]:
    with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8") as tf:
        tf.write(dot_str)
        tf.flush()
        dot_path = tf.name
        with Popen(
                ["node", path.join(path.dirname(__file__), "lib", "run.js"), "--dot", dot_path, "--mode", "identify"],
                stdout=PIPE, stderr=STDOUT) as process:
            process.wait()
            if process.returncode != 0:
                raise RuntimeError(process.stderr.read().decode("utf-8"))
            s = process.stdout.read().decode("utf-8")
    return identification_parse(s)


def estimate_direct_effect(estimator, data: pd.DataFrame, exposure: str, outcome: str, admissable: List[str],
                           *args, **kwargs):
    estimator.fit(data[[exposure, *admissable]], data[outcome], *args, **kwargs)
    return estimator


def predict_direct_effect(estimator, data: pd.DataFrame, exposure: str, admissable: List[str],
                          *args, **kwargs) -> pd.Series:
    return estimator.predict(data[[exposure, *admissable]], *args, **kwargs)
