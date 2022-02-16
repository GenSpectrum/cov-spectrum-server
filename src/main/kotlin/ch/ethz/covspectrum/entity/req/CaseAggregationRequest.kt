package ch.ethz.covspectrum.entity.req

import java.time.LocalDate

data class CaseAggregationRequest(
    var fields: List<CaseAggregationField>?,
    var region: String?,
    var country: String?,
    var division: String?,
    var dateFrom: LocalDate?,
    var dateTo: LocalDate?
)
