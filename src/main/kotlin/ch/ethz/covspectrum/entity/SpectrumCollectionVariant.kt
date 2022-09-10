package ch.ethz.covspectrum.entity

data class SpectrumCollectionVariant(
    val query: String,
    val name: String,
    val description: String,
    val highlighted: Boolean,
)
