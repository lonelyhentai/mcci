from subprocess import Popen, PIPE, STDOUT
from os import path
import json
from typing import List, Tuple
import tempfile
import pandas as pd

from libmcci.dowhy import Admissable, estimate, use


def find_instruments(dot_str: str) -> List[List[str]]:
    with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8") as tf:
        tf.write(dot_str)
        tf.flush()
        dot_path = tf.name
        with Popen(
                ["node", path.join(path.dirname(__file__), "lib", "run.js"), "--dot", dot_path, "--mode", "instrument"],
                stdout=PIPE, stderr=STDOUT) as process:
            process.wait()
            if process.returncode != 0:
                raise RuntimeError(process.stderr.read().decode("utf-8"))
            inst_groups: List[str] = json.loads(process.stdout.read().decode("utf-8"), encoding="utf-8")
    return list(map(lambda inst: list(filter(lambda a: a != "", inst.split(","))), inst_groups))


def estimate_instrument(to_outcome_estimators: List,
                        to_outcome_admissables: List[Admissable],
                        to_outcome_data: List[pd.DataFrame],
                        to_exposure_estimators: List,
                        to_exposure_admissables: List[Admissable],
                        to_exposure_data: List[pd.DataFrame],
                        *args, **kwargs) -> Tuple[List, List]:
    return (
        estimate(to_outcome_estimators, to_outcome_admissables, to_outcome_data, *args, **kwargs),
        estimate(to_exposure_estimators, to_exposure_admissables, to_exposure_data, *args, **kwargs))


def use_instrument(
                   to_outcome_estimators,
                   to_outcome_admissables: List[Admissable],
                   to_exposure_estimators,
                   to_exposure_admissables: List[Admissable],
                   test: pd.DataFrame,
                   estimator_fn,
                   *args, **kwargs) -> pd.DataFrame:
    (estimators, admissables) = estimator_fn(
        to_outcome_estimators,
        to_outcome_admissables,
        to_exposure_estimators,
        to_exposure_admissables,
        *args, **kwargs)
    return use(estimators, admissables, test, *args, **kwargs)