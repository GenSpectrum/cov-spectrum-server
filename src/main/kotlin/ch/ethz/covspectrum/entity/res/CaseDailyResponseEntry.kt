package ch.ethz.covspectrum.entity.res

import java.time.LocalDate

data class CaseDailyResponseEntry(
    var date: LocalDate?,
    var newCases: Int?,
    var newDeaths: Int?
)
