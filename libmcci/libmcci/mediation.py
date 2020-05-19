import pandas as pd


def gen_p(df: pd.DataFrame, count: str):
    def p(base, more=None) -> float:
        if more is None:
            return df.loc[base][count].sum() * 1.0 / df[count].sum()
        else:
            count_base = df.loc[base][count].sum()
            count_more = df.loc[base & more][count].sum()
            return count_more * 1.0 / count_base

    return p


def total_effect(df: pd.DataFrame, x_from, x_to, y, count_name="P", x_name: str = 'X', y_name: str = "Y") -> float:
    p = gen_p(df, count_name)
    return p(df[x_name] == x_to, df[y_name] == y) - p(df[x_name] == x_from, df[y_name] == y)


def nature_indirect_effect(df: pd.DataFrame,
                           do: str = "X", effect: str = "Y", mediation: str = "Z", count_column: str = None) -> float:
    pass


def nature_direct_effect(df: pd.DataFrame,
                         do: str = "X", effect: str = "Y", mediation: str = "Z", count_column: str = None) -> float:
    pass
