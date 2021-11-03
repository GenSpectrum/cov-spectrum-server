package ch.ethz.covspectrum.entity.res

data class Gene(
    val name: String,
    val startPosition: Int,
    val endPosition: Int,
    val aaSeq: String
)
