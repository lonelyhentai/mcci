from subprocess import Popen, PIPE
from os import path
import json
from typing import List, Tuple


def identification_parse(content: str) -> List[Tuple[str, str, List[List[str]]]]:
    identification: List[List[str, List[str]]] = json.loads(content, encoding="utf-8")
    res = []
    for i in identification:
        link = i[0].split('->')
        v1 = link[0]
        v2 = link[1]
        admissibles = list(map(lambda x: list(filter(lambda a: a != "", x.split(','))), i[1]))
        res.append((v1, v2, admissibles))
    return res


def identify(dot_path: str) -> List[Tuple[str, str, List[List[str]]]]:
    with Popen(
            ["node", path.join(path.dirname(__file__), "lib", "run.js"), "--dot", dot_path, "--mode", "identify"],
            stdout=PIPE) as process:
        s = process.stdout.read().decode("utf-8")
    return identification_parse(s)
