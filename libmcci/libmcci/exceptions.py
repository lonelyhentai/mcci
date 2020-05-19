from libmcci.model import CausalModel


class CausalModelNotDefinedError(Exception):
    def __init__(self):
        super().__init__(f"causal model {CausalModel.EXPOSURE} or {CausalModel.OUTCOME} not defined")


class NotIdentified(Exception):
    def __init__(self):
        super().__init__(f"not identified, can not estimate")
