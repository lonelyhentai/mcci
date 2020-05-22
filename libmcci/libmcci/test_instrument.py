import unittest

from libmcci.dowhy import Admissable
from libmcci.instrument import find_instruments, estimate_instrument
import numpy as np
import random
import pandas as pd
from sklearn import linear_model


class InstrumentTest(unittest.TestCase):
    def test_find_instrument(self):
        dot_str = """
        digraph {
        "A" []
        "Y" ["outcome"=""]
        "U" ["latent"=""]
        "X" ["exposure"=""]
        "A" -> "Y"
        "U" -> "X"
        "U" -> "Y"
        "X" -> "A"
        "Z" -> "X"
        }
    """
        temp_res = find_instruments(dot_str)
        self.assertCountEqual([["Z"]], temp_res)

    def test_estimate_and_use_instrument(self):
        SIZE = 1000
        U = 100 * np.random.normal(size=SIZE) + 100.0
        Z = np.array([1 if p < 0.5 else 0 for p in np.random.random(size=SIZE)])
        X = np.array([1 if random.random() * u < 20 else 0 for u in U]) * Z
        Y = np.array([1 if sub <= 0 else 0 for sub in (U + X * 15 - 20)])
        data = pd.DataFrame({'X': X, 'Y': Y, 'Z': Z})
        to_outcome_estimators = [linear_model.LinearRegression()]
        to_outcome_admissables = [Admissable('Z', 'Y', [])]
        to_exposure_estimators = [linear_model.LinearRegression()]
        to_exposure_admissables = [Admissable('Z', 'X', [])]
        estimate_instrument(to_outcome_estimators, to_outcome_admissables, [data],
                            to_exposure_estimators, to_exposure_admissables, [data])
        a = to_outcome_estimators[0].coef_[0]
        b = to_exposure_estimators[0].coef_[0]
        c = a / b
        self.assertTrue(c < 0)


if __name__ == '__main__':
    unittest.main()