package ch.ethz.covspectrum.entity.model.chen2021fitness

import java.time.LocalDate

data class LapisResponse (
    var data: List<Map<String, String>>,
    var errors: List<Any>,
    var info: LapisInfo
)

data class LapisInfo(
    var apiVersion: Int,
    var dataVersion: Long,
    var deprecationDate: LocalDate?,
    var deprecationInfo: String?
)
