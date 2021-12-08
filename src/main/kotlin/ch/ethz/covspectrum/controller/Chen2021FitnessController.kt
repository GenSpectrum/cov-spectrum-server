package ch.ethz.covspectrum.controller

import ApiResponse
import Daily
import Params
import PlotAbsoluteNumbers
import PlotProportion
import ValueWithCI
import ch.ethz.covspectrum.entity.model.chen2021fitness.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate
import java.time.temporal.ChronoUnit


const val LAPIS_ENDPOINT = "https://lapis.cov-spectrum.org/gisaid/v1/sample/aggregated";
const val MODEL_ENDPOINT = "http://cov-spectrum-model-chen2021Fitness:7070/with-prediction";
//const val MODEL_ENDPOINT = "http://localhost:7070/with-prediction";

@RestController
@RequestMapping("/computed/model/chen2021Fitness")
class Chen2021FitnessController(
    restTemplateBuilder: RestTemplateBuilder
) {
    private val restTemplate: RestTemplate = restTemplateBuilder.build();

    @GetMapping("")
    fun compute(req: ApiRequest): ApiResponse {
        // Fetch sequencing data from LAPIS
        var builder = UriComponentsBuilder.newInstance()
            .uri(URI(LAPIS_ENDPOINT))
            .queryParam("fields", "date")
            .queryParam("dateFrom", req.plotStartDate)
            .queryParam("dateTo", req.plotEndDate)
        req.region?.let { builder = builder.queryParam("region", it) }
        req.country?.let { builder = builder.queryParam("country", it) }
        req.division?.let { builder = builder.queryParam("division", it) }
        req.samplingStrategy?.let { builder = builder.queryParam("samplingStrategy", it) }
        val wholeDatasetUrl = builder.build().encode().toUri();
        req.pangoLineage?.let { builder = builder.queryParam("pangoLineage", it) }
        req.gisaidClade?.let { builder = builder.queryParam("gisaidClade", it) }
        req.nextstrainClade?.let { builder = builder.queryParam("nextstrainClade", it) }
        req.aaMutations?.let { builder = builder.queryParam("aaMutations", it) }
        req.nucMutations?.let { builder = builder.queryParam("nucMutations", it) }
        req.variantQuery?.let { builder = builder.queryParam("variantQuery", it) }
        val variantDatasetUrl = builder.build().encode().toUri()
        val wholeDataset: LapisResponse = restTemplate.getForObject(wholeDatasetUrl)
        val variantDataset: LapisResponse = restTemplate.getForObject(variantDatasetUrl)
        check(wholeDataset.errors.isEmpty() && variantDataset.errors.isEmpty())
        if (wholeDataset.info.dataVersion != variantDataset.info.dataVersion) {
            TODO("Repeat")
        }

        // Compute the transmission fitness advantage through the model endpoint.
        // We need a mapping from calendar days to t. plotStartDate shall be t=0.
        val t0 = req.plotStartDate
        val variantCountByDate = mutableMapOf<LocalDate, Int>()
        for (e in variantDataset.data) {
            e.date?.let { variantCountByDate.put(it, e.count) }
        }
        val t = mutableListOf<Int>()
        val n = mutableListOf<Int>()
        val k = mutableListOf<Int>()
        for (e in wholeDataset.data) {
            if (e.date == null) continue
            t.add(ChronoUnit.DAYS.between(t0, e.date).toInt())
            n.add(e.count)
            k.add(variantCountByDate.get(e.date) ?: 0)
        }
        val modelReq = PythonModelRequest(
            InnerData(t, n, k),
            req.alpha,
            req.generationTime,
            req.reproductionNumberWildtype,
            0,
            ChronoUnit.DAYS.between(t0, req.plotEndDate).toInt(),
            req.initialWildtypeCases,
            req.initialVariantCases
        )
        val modelReqJson = ObjectMapper().writeValueAsString(modelReq)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON;
        val modelHttpReq = HttpEntity(modelReqJson, headers)
        val modelRes = restTemplate.postForObject(MODEL_ENDPOINT, modelHttpReq, PythonModelResponse::class.java)
        check(modelRes != null)

        // Map to the final response object
        return ApiResponse(
            Daily(
                modelRes.daily.t.map { t0.plusDays(it.toLong()) },
                modelRes.daily.proportion,
                modelRes.daily.ci_lower,
                modelRes.daily.ci_upper
            ),
            Params(
                ValueWithCI(modelRes.params.a.value, modelRes.params.a.ci_lower, modelRes.params.a.ci_upper),
                ValueWithCI(modelRes.params.t0.value, modelRes.params.t0.ci_lower, modelRes.params.t0.ci_upper),
                ValueWithCI(modelRes.params.fc.value, modelRes.params.fc.ci_lower, modelRes.params.fc.ci_upper),
                ValueWithCI(modelRes.params.fd.value, modelRes.params.fd.ci_lower, modelRes.params.fd.ci_upper)
            ),
            PlotAbsoluteNumbers(
                modelRes.plot_absolute_numbers.t.map { t0.plusDays(it.toLong()) },
                modelRes.plot_absolute_numbers.variant_cases,
                modelRes.plot_absolute_numbers.wildtype_cases
            ),
            PlotProportion
                (modelRes.plot_proportion.t.map { t0.plusDays(it.toLong()) },
                modelRes.plot_proportion.proportion,
                modelRes.plot_proportion.ci_lower,
                modelRes.plot_proportion.ci_upper
            )
        )
    }

}
