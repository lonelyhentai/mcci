import unittest
import tempfile
import os
from .instrument import find_instruments


class InstrumentTest(unittest.TestCase):
    def test_find_instrument(self):
        dot_content = """
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
        with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8", delete=False) as tf:
            tf.write(dot_content)
            dot_path = tf.name
            tf.flush()
        temp_res = find_instruments(dot_path)
        print(temp_res)
        self.assertCountEqual([["Z"]], temp_res)
        os.remove(dot_path)


if __name__ == '__main__':
    unittest.main()
