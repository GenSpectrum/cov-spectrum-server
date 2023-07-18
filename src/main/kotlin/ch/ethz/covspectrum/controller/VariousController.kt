package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.entity.req.CaseAggregationRequest
import ch.ethz.covspectrum.entity.res.CaseAggregationResponse
import ch.ethz.covspectrum.entity.res.CountryMappingResponseEntry
import ch.ethz.covspectrum.entity.res.PangoLineageRecombinant
import ch.ethz.covspectrum.service.DatabaseService
import ch.ethz.covspectrum.util.PangoLineageAlias
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

val defaultCacheControl = CacheControl.maxAge(12, TimeUnit.HOURS).cachePublic()

@RestController
class VariousController(
    private val databaseService: DatabaseService
) {
    @GetMapping("/resource/country")
    fun getCountryMapping(): List<CountryMappingResponseEntry> {
        return databaseService.getCountryMapping()
    }

    @GetMapping("/resource/case")
    fun getCases(req: CaseAggregationRequest): ResponseEntity<CaseAggregationResponse> {
        val body = databaseService.getCases(req)
        return ResponseEntity.ok()
            .cacheControl(defaultCacheControl)
            .body(body);
    }

    @GetMapping("/resource/pango-lineage-alias")
    fun getPangoLineageAliases(): List<PangoLineageAlias> {
        return databaseService.getPangoLineageAliases()
    }

    @GetMapping("/resource/pango-lineage-recombinant")
    fun getPangoLineageRecombinants(): List<PangoLineageRecombinant> {
        return databaseService.getPangoLineageRecombinants()
    }

    @GetMapping(value = ["/resource/wastewater"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getWastewaterResults(region: String?, country: String?, division: String?): String {
        return databaseService.getWastewaterResults(region, country, division)
    }

    @GetMapping(value = ["/resource/wastewater-viloca"], produces = ["image/svg+xml"])
    fun getWastewaterVILOCAPlot(): String {
        return databaseService.getWastewaterVILOCAPlot()
    }
}
