package ch.ethz.covspectrum.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun nowUTCDate(): LocalDate {
    return LocalDate.now(ZoneId.of("UTC"))
}

fun nowUTCDateTime(): LocalDateTime {
    return LocalDateTime.now(ZoneId.of("UTC"))
}
