from dataclasses import dataclass
import numpy as np


@dataclass(frozen=True)
class Input:
    alpha: float
    generation_time: float
    reproduction_number_wildtype: float
    data_t: np.ndarray
    data_n: np.ndarray
    data_k: np.ndarray
    plot_start_t: int
    plot_end_t: int
    initial_wildtype: int
    initial_variant: int


@dataclass(frozen=True)
class ValueWithCi:
    value: float
    ci_lower: float
    ci_upper: float


@dataclass(frozen=True)
class ValueWithCi2:
    value: np.ndarray
    ci_lower: np.ndarray
    ci_upper: np.ndarray


@dataclass(frozen=True)
class GrowthNumbers:
    a: ValueWithCi
    t0: ValueWithCi
    fd: ValueWithCi
    fc: ValueWithCi


@dataclass(frozen=True)
class PlotAbsoluteNumbers:
    t: np.ndarray
    wildtype_cases: np.ndarray
    variant_cases: np.ndarray


@dataclass(frozen=True)
class PlotProportion:
    t: np.ndarray
    proportion: np.ndarray
    ci_lower: np.ndarray
    ci_upper: np.ndarray


@dataclass(frozen=True)
class PointData:
    t: np.ndarray
    proportion: np.ndarray
    ci_lower: np.ndarray
    ci_upper: np.ndarray


@dataclass()
class Result:
    params: GrowthNumbers
    plot_absolute_numbers: PlotAbsoluteNumbers
    plot_proportion: PlotProportion
    daily: PointData
