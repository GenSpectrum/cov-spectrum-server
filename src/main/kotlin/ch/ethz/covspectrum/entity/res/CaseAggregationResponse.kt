package ch.ethz.covspectrum.entity.res

import ch.ethz.covspectrum.entity.req.CaseAggregationField


data class CaseAggregationResponse(
    val fields: List<CaseAggregationField>,
    val entries: List<CaseAggregationResponseEntry>
)
