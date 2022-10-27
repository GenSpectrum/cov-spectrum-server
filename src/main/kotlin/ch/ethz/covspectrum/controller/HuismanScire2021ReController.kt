package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.service.DatabaseService
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.xml.bind.DatatypeConverter
import kotlin.concurrent.thread

val currentCalculations = ConcurrentHashMap<String, Boolean>() // We store the hash in the key and ignore the value
const val MAX_PARALLEL_CALCULATIONS = 8
const val URL = "http://cov-spectrum-server-nginx:80/model-huisman_scire2021re/get-re"

@RestController
@RequestMapping("/computed/model/huismanScire2021Re")
class HuismanScire2021ReController(
    private val databaseService: DatabaseService,
    restTemplateBuilder: RestTemplateBuilder
) {
    private val restTemplate: RestTemplate = restTemplateBuilder.build()

    @PostMapping("/get-result", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getResult(@RequestBody body: String): String {
        // Hash body
        val hash = hashBody(body)

        // Lookup hash: If the result is available, return it. If not, say that (but do not calculate)
        val result = databaseService.getHuismanScire2021ReResult(hash)
        val state = if (result != null) (if (result.first) "RESULT_AVAILABLE" else "CALCULATION_FAILED")
        else "RESULT_UNAVAILABLE"
        return """{"state":"$state","result":${result?.second}}"""
    }

    @PostMapping("/calculate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun calculate(@RequestBody body: String): String {
        // Hash body
        val hash = hashBody(body)

        // If the result is already available, no need to do more
        val existingResult = databaseService.getHuismanScire2021ReResult(hash)
        if (existingResult != null) {
            return """{"state": "OK"}"""
        }

        // If the result is already getting calculated, also nothing to do
        if (currentCalculations.containsKey(hash)) {
            return """{"state": "OK"}"""
        }

        // If there are already many ongoing calculations, reject
        if (currentCalculations.size > MAX_PARALLEL_CALCULATIONS) {
            return """{"state": "REJECTED_FULL_QUEUE"}"""
        }

        // Otherwise, initiate calculation
        currentCalculations[hash] = true
        thread {
            val startTime = System.currentTimeMillis();
            try {
                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON;
                val modelHttpReq = HttpEntity(body, headers)
                val result = restTemplate.postForObject(URL, modelHttpReq, String::class.java)

                // Store the calculated result
                val duration = (System.currentTimeMillis() - startTime).toInt() / 1000
                databaseService.insertHuismanScire2021ReResult(hash, duration, body, true, result)
            } catch (e: Exception) {
                val duration = (System.currentTimeMillis() - startTime).toInt() / 1000
                databaseService.insertHuismanScire2021ReResult(hash, duration, body, false, null)
            } finally {
                currentCalculations.remove(hash)
            }
        }

        return """{"state": "OK"}"""
    }

    private fun hashBody(body: String): String {
        val md = MessageDigest.getInstance("sha256")
        md.update(body.toByteArray())
        val digest = md.digest()
        return DatatypeConverter.printHexBinary(digest).uppercase(Locale.getDefault())
    }

}
