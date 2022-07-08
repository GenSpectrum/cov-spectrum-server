package ch.ethz.covspectrum.entity

data class SpectrumCollection(
    var id: Int?,
    val title: String,
    val description: String,
    val maintainers: String,
    val email: String,
    val variants: MutableList<SpectrumCollectionVariant>,
)
