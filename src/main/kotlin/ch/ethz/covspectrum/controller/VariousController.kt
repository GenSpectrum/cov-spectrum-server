package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.entity.req.CaseAggregationRequest
import ch.ethz.covspectrum.entity.res.CaseAggregationResponse
import ch.ethz.covspectrum.entity.res.CountryMappingResponseEntry
import ch.ethz.covspectrum.entity.res.RxivArticleResponseEntry
import ch.ethz.covspectrum.service.DatabaseService
import ch.ethz.covspectrum.util.PangoLineageAlias
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

    @GetMapping("/resource/case")
    fun getCases(req: CaseAggregationRequest): CaseAggregationResponse {
        return databaseService.getCases(req)
    }

    @GetMapping("/resource/article")
    fun getPangoLineageArticles(pangoLineage: String): List<RxivArticleResponseEntry> {
        return databaseService.getPangoLineageArticles(pangoLineage)
    }

    @GetMapping("/resource/pango-lineage-alias")
    fun getPangoLineageAliases(): List<PangoLineageAlias> {
        return databaseService.getPangolinLineageAliases();
    }
}
