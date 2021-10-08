package ch.ethz.covspectrum.entity.req

data class CaseAggregationRequest(
    var fields: List<CaseAggregationField>?,
    var region: String?,
    var country: String?
)
