package ch.ethz.covspectrum.entity.res

import java.time.LocalDate

data class CaseAggregationResponseEntry(
    var region: String?,
    var country: String?,
    var division: String?,
    var date: LocalDate?,
    var newCases: Int?,
    var newDeaths: Int?
)
