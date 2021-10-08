package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.entity.req.CaseDailyRequest
import ch.ethz.covspectrum.entity.res.CaseDailyResponseEntry
import ch.ethz.covspectrum.entity.res.CountryMappingResponseEntry
import ch.ethz.covspectrum.entity.res.RxivArticleResponseEntry
import ch.ethz.covspectrum.service.DatabaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VariousController(
    private val databaseService: DatabaseService
) {
    @GetMapping("/resource/country")
    fun getCountryMapping(): List<CountryMappingResponseEntry> {
        return databaseService.getCountryMapping();
    }

    @GetMapping("/resource/case/daily")
    fun getDailyCases(req: CaseDailyRequest): List<CaseDailyResponseEntry> {
        return databaseService.getDailyCases(req)
    }

    @GetMapping("/resource/article")
    fun getPangoLineageArticles(pangoLineage: String): List<RxivArticleResponseEntry> {
        return databaseService.getPangoLineageArticles(pangoLineage)
    }
}
