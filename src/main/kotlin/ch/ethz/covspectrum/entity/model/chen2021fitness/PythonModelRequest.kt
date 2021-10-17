package ch.ethz.covspectrum.entity.model.chen2021fitness

data class PythonModelRequest(
    var data: InnerData,
    var alpha: Double,
    var generationTime: Double,
    var reproductionNumberWildtype: Double,
    var plotStartT: Int,
    var plotEndT: Int,
    var initialWildtype: Int,
    var initialVariant: Int
)

data class InnerData (
    var t: List<Int>,
    var n: List<Int>,
    var k: List<Int>
)
