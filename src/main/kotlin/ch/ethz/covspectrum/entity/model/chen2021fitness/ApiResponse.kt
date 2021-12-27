import ch.ethz.covspectrum.entity.model.chen2021fitness.ChangePointResult
import java.time.LocalDate

data class ApiResponse(
    var daily: Daily,
    var params: Params,
    var plotAbsoluteNumbers: PlotAbsoluteNumbers,
    var plotProportion: PlotProportion,
    var changePoints: List<ChangePointResult>?
)

data class ValueWithCI (
    var value: Double,
    var ciLower: Double,
    var ciUpper: Double
)

data class Daily (
    var t: List<LocalDate>,
    var proportion: List<Double>,
    var ciLower: List<Double>,
    var ciUpper: List<Double>
)

data class Params (
    var a: ValueWithCI,
    var t0: ValueWithCI,
    var fc: ValueWithCI,
    var fd: ValueWithCI
)

data class PlotAbsoluteNumbers (
    var t: List<LocalDate>,
    var variantCases: List<Int>,
    var wildtypeCases: List<Int>
)

data class PlotProportion (
    var t: List<LocalDate>,
    var proportion: List<Double>,
    var ciLower: List<Double>,
    var ciUpper: List<Double>
)
