package ch.ethz.covspectrum.entity.res

data class ReferenceGenomeResponse(
    var nucSeq: String,
    var genes: List<Gene>
)
