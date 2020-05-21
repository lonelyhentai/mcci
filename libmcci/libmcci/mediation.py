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


def total_effect(df: pd.DataFrame, exposure_from, exposure_to, outcome,
                 exposure_col: str = 'X', outcome_col: str = "Y", count_col: str = "P") \
        -> float:
    p = gen_p(df, count_col)
    return (p(df[exposure_col] == exposure_to, df[outcome_col] == outcome) -
            p(df[exposure_col] == exposure_from, df[outcome_col] == outcome))


def nature_indirect_effect(df: pd.DataFrame, exposure_from, exposure_to, outcome,
                           exposure_col: str = "X", outcome_col: str = "Y",
                           mediation_col: str = "Z", count_col: str = "P") -> float:
    p = gen_p(df, count_col)
    res = 0
    for m in df[mediation_col].astype('category').dtypes.categories.to_list():
        res += ((p(df[exposure_col] == exposure_to, df[mediation_col] == m)
                 - p(df[exposure_col] == exposure_from, df[mediation_col] == m))
                * p((df[exposure_col] == exposure_from) & (df[mediation_col] == m),
                    df[outcome_col] == outcome))
    return res


def nature_direct_effect(df: pd.DataFrame, exposure_from, exposure_to, outcome,
                         exposure_col: str = "X", outcome_col: str = "Y",
                         mediation_col: str = "Z", count_col: str = "P") -> float:
    return (total_effect(df, exposure_from, exposure_to,
                         outcome, exposure_col, outcome_col, count_col) +
            nature_indirect_effect(df, exposure_to, exposure_from, outcome,
                                   exposure_col, outcome_col, mediation_col, count_col))


def controlled_direct_effect(df: pd.DataFrame, exposure_from, exposure_to,
                             outcome, controlled_mediation, exposure_col: str = "X",
                             outcome_col: str = "Y", mediation_col: str = "Z",
                             count_col: str = "P") -> float:
    p = gen_p(df, count_col)
    return (p((df[exposure_col] == exposure_to) & (df[mediation_col] == controlled_mediation),
              df[outcome_col] == outcome) -
            p((df[exposure_col] == exposure_from) & (df[mediation_col] == controlled_mediation),
              df[outcome_col] == outcome))


def group_by_count(df: pd.DataFrame, exposure_col: str = "X", outcome_col: str = "Y",
                   mediation_col: str = "Z", count_col: str = "P") -> pd.DataFrame:
    res = []
    for (g, c) in df.groupby([exposure_col, outcome_col, mediation_col]):
        res.append([*list(g), len(c)])
    return pd.DataFrame(res, columns=[exposure_col, outcome_col, mediation_col, count_col])
