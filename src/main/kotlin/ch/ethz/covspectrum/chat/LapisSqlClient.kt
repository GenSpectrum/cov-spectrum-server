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
        val url = "${host}/sample/sqlForChat?accessKey=$accessKey"
        val req = HttpEntity(sql)
        val response = RestTemplate().postForObject(url, req, Map::class.java)
        if (response?.get("error") != null) {
            throw UnsupportedSqlException(sql)
        }

        return response?.get("data") as List<Map<Any, Any>>
    }
}

fun main() {
    val lapis = LapisSqlClient("http://localhost:2345/v1", null)

    val sqls = listOf(
        """
            select date, count(*) as num
            from metadata
            where country = 'Switzerland' and lineage = 'B.1.1.7'
            group by date
            having num > 2
            order by num asc
            limit 1
            offset 2;
        """.trimIndent(),
        """
            select date, count(*) as count
            from metadata
            where country = 'Switzerland'
            group by date
            order by date
            limit 1;
        """.trimIndent(),
        """
            select country, count(*) as count
            from metadata
            where lineage = 'BA.1'
            group by country
            order by count desc
            limit 1;
        """.trimIndent(),
        """
            select date, count(*) as count
            from metadata
            where country = 'Switzerland' and region = 'Asia';
        """.trimIndent(),
        """
            select date, count(*) as count
            from metadata
            where country = 'Switzerland' and region = 'Asia';
        """.trimIndent(),
        """
            select count(*)
            from lapis
            where country = 'Australia' and nuc_25563 = 'T' and aa_N_377 = 'Y';
        """.trimIndent()
    )
    for (sql in sqls) {
        val data = lapis.execute(sql)
        println("Next..")
    }
}
