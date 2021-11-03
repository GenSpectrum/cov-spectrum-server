package ch.ethz.covspectrum.entity.res

import java.time.LocalDate


data class RxivArticleResponseEntry(
    var doi: String?,
    var title: String,
    var authors: List<String>?,
    var date: LocalDate?,
    var category: String?,
    var published: String?,
    var server: String?,
    var abstractText: String?
)
