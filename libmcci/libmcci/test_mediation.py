import unittest
import pandas as pd
from libmcci.mediation import group_by_count, nature_indirect_effect, \
    total_effect, nature_direct_effect, controlled_direct_effect


class MediationTest(unittest.TestCase):
    def test_group_by_count(self):
        source = pd.DataFrame([
            [1, 1, 0],
            [0, 0, 1],
            [1, 1, 0]
        ], columns=["A", "B", "C"])
        target = group_by_count(source, "A", "B", "C", "D")
        expected = pd.DataFrame([
            [1, 1, 0, 2],
            [0, 0, 1, 1]
        ], columns=["A", "B", "C", "D"])
        self.assertEqual(
            set(map(tuple, target)),
            set(map(tuple, expected))
        )

    def test_mediation(self):
        data = pd.DataFrame([
            [0, 0, 0, 9900],
            [0, 0, 1, 100],
            [0, 1, 0, 490],
            [0, 1, 1, 10],
            [1, 0, 0, 4850],
            [1, 0, 1, 150],
            [1, 1, 0, 800],
            [1, 1, 1, 200]
        ], columns=["X", "Z", "Y", "P"])
        te01 = total_effect(data, 0, 1, 1)
        self.assertAlmostEqual(te01, 0.04785, places=4)
        nie01 = nature_indirect_effect(data, 0, 1, 1)
        self.assertAlmostEqual(nie01, 0.0012, places=4)
        nie10 = nature_indirect_effect(data, 1, 0, 1)
        self.assertAlmostEqual(nie10, -0.0202, places=4)
        nde01 = nature_direct_effect(data, 0, 1, 1)
        self.assertAlmostEqual(nde01, 0.0276, places=4)
        cde01 = controlled_direct_effect(data, 0, 1, 1, 0)
        self.assertAlmostEqual(cde01, 0.01999, places=4)


if __name__ == '__main__':
    unittest.main()
