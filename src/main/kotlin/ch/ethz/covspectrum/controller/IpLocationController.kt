package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.entity.res.IpCountryResponse
import ch.ethz.covspectrum.service.DatabaseService
import com.maxmind.geoip2.DatabaseReader
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.net.InetAddress
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/internal")
class IpLocationController(
    private val databaseService: DatabaseService
) {

    private val geoLite2DBFilePath: String? = System.getenv("COV_SPECTRUM_GEO_LITE2_DB_PATH");
    private val ipDatabaseReader: DatabaseReader? = if (geoLite2DBFilePath == null) null else
        DatabaseReader.Builder(File(geoLite2DBFilePath)).build()
    private val isoToCountryMap = fetchIsoToCountryMapping()

    @RequestMapping(value = ["/my-country"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMyCountry(request: HttpServletRequest): IpCountryResponse {
        if (ipDatabaseReader == null) {
            return IpCountryResponse(null, null)
        }
        // If the server is running behind Cloudflare, the country of the user is in the "CF-IPCountry" header field.
        // https://support.cloudflare.com/hc/en-us/articles/200168236-Configuring-Cloudflare-IP-Geolocation
        val cfCountry = request.getHeader("CF-IPCountry")
        if (cfCountry != null) {
            return isoToCountryMap[cfCountry] ?: IpCountryResponse(null, null)
        }
        // If the server is running behind a reverse proxy, it is required that the reverse proxy sets the
        // X-Real-IP header to the IP address of the visitor.
        try {
            val ipAddressString = request.getHeader("X-Real-IP") ?: request.remoteAddr
            val ipAddress = InetAddress.getByName(ipAddressString)
            val countryResponse = ipDatabaseReader.country(ipAddress)
            return IpCountryResponse(countryResponse.continent.names["en"], countryResponse.country.names["en"])
        } catch (e: Exception) {
            return IpCountryResponse(null, null)
        }
    }

    private fun fetchIsoToCountryMapping(): Map<String, IpCountryResponse> {
        val mapping = mutableMapOf<String, IpCountryResponse>();
        val sql = """
            select distinct
              iso_alpha_2,
              cov_spectrum_country,
              cov_spectrum_region
            from country_mapping
            where cov_spectrum_country is not null and iso_alpha_2 is not null;
        """.trimIndent()
        databaseService.getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    while (rs.next()) {
                        mapping.put(
                            rs.getString("iso_alpha_2"),
                            IpCountryResponse(
                                rs.getString("cov_spectrum_region"),
                                rs.getString("cov_spectrum_country")
                            )
                        )
                    }
                }
            }
        }
        return mapping
    }

}
