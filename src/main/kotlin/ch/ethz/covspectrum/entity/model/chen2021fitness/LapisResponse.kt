package ch.ethz.covspectrum.entity.model.chen2021fitness

import java.time.LocalDate

data class LapisResponse (
    var payload: List<PayloadEntry>,
    var errors: List<Any>,
    var info: LapisInfo
)

data class PayloadEntry(
    var date: LocalDate?,
    var count: Int
)

data class LapisInfo(
    var apiVersion: Int,
    var dataVersion: Long,
    var deprecationDate: LocalDate?,
    var deprecationInfo: String?
)
