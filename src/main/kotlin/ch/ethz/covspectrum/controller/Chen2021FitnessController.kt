package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.util.FifoCache
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate


const val MODEL_ENDPOINT = "https://dev.cov-spectrum.org/api/model-chen2021fitness"

@RestController
@RequestMapping("/computed/model/chen2021Fitness")
class Chen2021FitnessController(
    restTemplateBuilder: RestTemplateBuilder
) {
    private val restTemplate: RestTemplate = restTemplateBuilder.build();
    private val cache = FifoCache<String, String>(5000)

    @RequestMapping(
        value = [""],
        method = [RequestMethod.POST],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    fun compute(@RequestBody body: String): String {
        // Check cache
        val cached = cache.get(body)
        if (cached != null) {
            return cached
        }

        // Call model endpoint to calculate value
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity<String>(body, headers)
        val result = restTemplate.postForObject(MODEL_ENDPOINT, entity, String::class.java)!!

        // Save value in the cache
        cache.put(body, result)

        // Done
        return result
    }

}
