package ch.ethz.covspectrum.entity.res

data class CountryMappingResponseEntry(
    var covSpectrumName: String,
    var gisaidName: String,
    var owidName: String?,
    var region: String
)
