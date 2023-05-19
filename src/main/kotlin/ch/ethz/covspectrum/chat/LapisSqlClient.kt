package ch.ethz.covspectrum.chat

import org.springframework.http.HttpEntity
import org.springframework.web.client.RestTemplate


/**
 * This uses LAPIS' dedicated /sqlForChat endpoint.
 */
class LapisSqlClient(
    private val host: String,
    private val accessKey: String?
) {
    fun execute(sql: String): List<Map<Any, Any>> {
        val url = if (accessKey != null) "${host}/sample/sqlForChat?accessKey=$accessKey" else
            "${host}/sample/sqlForChat?"
        val req = HttpEntity(sql)
        val response = RestTemplate().postForObject(url, req, Map::class.java)
        if (response?.get("error") != null) {
            throw UnsupportedSqlException(sql)
        }

        return response?.get("data") as List<Map<Any, Any>>
    }
}
