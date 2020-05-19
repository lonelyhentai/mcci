import unittest
from libmcci.instrument import find_instruments


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


if __name__ == '__main__':
    unittest.main()
