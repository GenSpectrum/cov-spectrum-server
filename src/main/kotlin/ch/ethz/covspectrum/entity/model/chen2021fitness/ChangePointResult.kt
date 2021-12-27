package ch.ethz.covspectrum.entity.model.chen2021fitness

import ValueWithCI
import java.time.LocalDate

data class ChangePointResult (
    var t: LocalDate,
    var fc: ValueWithCI
)
