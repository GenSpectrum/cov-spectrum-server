package ch.ethz.covspectrum.entity.model.chen2021fitness

import java.time.LocalDate

data class ApiRequest(
    var region: String?,
    var country: String?,
    var division: String?,
    var pangoLineage: String?,
    var gisaidClade: String?,
    var nextstrainClade: String?,
    var aaMutations: String?,
    var nucMutations: String?,
    var alpha: Double = 0.95,
    var generationTime: Double = 4.8,
    var reproductionNumberWildtype: Double = 1.0,
    var plotStartDate: LocalDate,
    var plotEndDate: LocalDate,
    var initialWildtypeCases: Int = 1000,
    var initialVariantCases: Int = 100
)
