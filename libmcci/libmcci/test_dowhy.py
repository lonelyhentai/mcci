import unittest
from libmcci.dowhy import identify, estimate_atomic_effect, DirectIdentification, use_atomic_effect, Admissable, \
    estimate, use
from sklearn import svm
import pandas as pd
import numpy as np


class DoWhyTest(unittest.TestCase):
    def test_identify(self):
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
    }
"""
        res = identify(dot_str)
        expected = [DirectIdentification('X', 'A', [[]]), DirectIdentification('A', 'Y', [['X']])]
        self.assertCountEqual(res, expected)

    def test_estimate_and_use_atomic_effect(self):
        SIZE = 1000
        U = 20 * np.random.normal(size=SIZE)
        X = np.random.normal(size=SIZE) + U
        A = np.array([100.0 if x > 10 else 0.0 for x in X])
        Y = U + A + np.random.normal(size=SIZE)
        data = pd.DataFrame({'X': X, 'Y': Y, 'A': A})
        estimator_x_a = estimate_atomic_effect(svm.SVR(kernel="rbf"), Admissable('X', 'A', []), data)
        estimator_a_y = estimate_atomic_effect(svm.SVR(kernel="rbf"), Admissable('A', 'Y', ['X']), data)
        data_x = pd.DataFrame({'X': [20.], 'Y': [120.], 'A': [100.]})
        test_x = pd.DataFrame({'X': [20.]})
        res = use_atomic_effect(
            estimator_a_y,
            Admissable('A', 'Y', ['X']),
            pd.DataFrame(
                {'A': use_atomic_effect(estimator_x_a, Admissable('X', 'A', []), test_x[['X']]), 'X': test_x['X']}))
        expected = data_x.loc[0, 'Y']
        self.assertTrue(expected - 10.0 < res[0] < expected + 10.0)

    def test_estimate_and_use(self):
        SIZE = 1000
        U = 20 * np.random.normal(size=SIZE)
        X = np.random.normal(size=SIZE) + U
        A = np.array([100.0 if x > 10 else 0.0 for x in X])
        Y = U + A + np.random.normal(size=SIZE)
        data = pd.DataFrame({'X': X, 'Y': Y, 'A': A})
        admissables = [Admissable('X', 'A', []), Admissable('A', 'Y', ['X'])]
        data_x = pd.DataFrame({'X': [20.], 'Y': [120.], 'A': [100.]})
        test_x = pd.DataFrame({'X': [20.]})
        estimators = estimate([svm.SVR(kernel="rbf"), svm.SVR(kernel="rbf")], admissables, [data, data])
        res = use(estimators, admissables, test_x)
        expected = data_x.loc[0, 'Y']
        self.assertTrue(expected - 10.0 < res.loc[0, 'Y'] < expected + 10.0)


if __name__ == '__main__':
    unittest.main()
