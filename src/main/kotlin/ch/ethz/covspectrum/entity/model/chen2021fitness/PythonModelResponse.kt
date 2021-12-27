package ch.ethz.covspectrum.entity.model.chen2021fitness

data class PythonModelResponse (
    var daily: Daily,
    var params: Params,
    var plot_absolute_numbers: PlotAbsoluteNumbers,
    var plot_proportion: PlotProportion
)

data class PythonValueWithCI (
    var value: Double,
    var ci_lower: Double,
    var ci_upper: Double
)

data class Daily (
    var t: List<Int>,
    var proportion: List<Double>,
    var ci_lower: List<Double>,
    var ci_upper: List<Double>
)

data class Params (
    var a: PythonValueWithCI,
    var t0: PythonValueWithCI,
    var fc: PythonValueWithCI,
    var fd: PythonValueWithCI
)

data class PlotAbsoluteNumbers (
    var t: List<Int>,
    var variant_cases: List<Int>,
    var wildtype_cases: List<Int>
)

data class PlotProportion (
    var t: List<Int>,
    var proportion: List<Double>,
    var ci_lower: List<Double>,
    var ci_upper: List<Double>
)
