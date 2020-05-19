from subprocess import Popen, PIPE, STDOUT
from os import path
import json
from typing import List
import tempfile


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
