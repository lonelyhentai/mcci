class CausalModelNotDefinedError(Exception):
    def __init__(self):
        super().__init__(f"causal model exposure or outcome not defined")


class NotIdentified(Exception):
    def __init__(self):
        super().__init__(f"not identified, can not estimate")
