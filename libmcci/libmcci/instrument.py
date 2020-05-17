from subprocess import Popen, PIPE
from os import path
import json
from typing import List


def find_instruments(dot_path: str) -> List[List[str]]:
    with Popen(
            ["node", path.join(path.dirname(__file__), "lib", "run.js"), "--dot", dot_path, "--mode", "instrument"],
            stdout=PIPE) as process:
        inst_groups: List[str] = json.loads(process.stdout.read().decode("utf-8"), encoding="utf-8")
    return list(map(lambda inst: list(filter(lambda a: a != "", inst.split(","))), inst_groups))
