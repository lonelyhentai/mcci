import unittest
from libmcci.dowhy import identify, estimate_direct_effect, DirectIdentification, predict_direct_effect
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

    def test_estimate_and_predict_direct_effect(self):
        SIZE = 2000
        U = 20 * np.random.normal(size=SIZE)
        X = np.random.normal(size=SIZE) + U
        A = np.array([100.0 if x > 10 else 0.0 for x in X])
        Y = U + A + np.random.normal(size=SIZE)
        data = pd.DataFrame({'X': X, 'Y': Y, 'A': A})
        estimator_x_a = estimate_direct_effect(svm.SVR(kernel="rbf"), data, 'X', 'A', [])
        estimator_a_y = estimate_direct_effect(svm.SVR(kernel="rbf"), data, 'A', 'Y', ['X'])
        test_x = pd.DataFrame({'X': [20.], 'Y': [120.], 'A': [100.]})
        res = predict_direct_effect(
            estimator_a_y,
            pd.DataFrame({'A': predict_direct_effect(estimator_x_a, test_x[['X']], 'X', []), 'X': test_x['X']}),
            'A', ['X'])
        self.assertTrue(110.0 < res[0] < 130.0)


if __name__ == '__main__':
    unittest.main()
