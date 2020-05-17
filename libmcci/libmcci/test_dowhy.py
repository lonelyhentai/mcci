import unittest
import tempfile
import os
from .dowhy import identify


class DoWhyTest(unittest.TestCase):
    def test_identify(self):
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
    }
"""
        with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8", delete=False) as tf:
            tf.write(dot_content)
            dot_path = tf.name
            tf.flush()
        temp_res = identify(dot_path)
        print(temp_res)
        self.assertCountEqual([('X', 'A', [[]]), ("A", "Y", [["X"]])], temp_res)
        os.remove(dot_path)


if __name__ == '__main__':
    unittest.main()
