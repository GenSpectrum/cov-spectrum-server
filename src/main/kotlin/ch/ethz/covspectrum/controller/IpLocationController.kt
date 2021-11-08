package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.entity.res.IpCountryResponse
import com.maxmind.geoip2.DatabaseReader
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.net.InetAddress
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/internal")
class IpLocationController {

    private val geoLite2DBFilePath: String? = System.getenv("COV_SPECTRUM_GEO_LITE2_DB_PATH");
    private val ipDatabaseReader: DatabaseReader? = if (geoLite2DBFilePath == null) null else
        DatabaseReader.Builder(File(geoLite2DBFilePath)).build()

    @RequestMapping(value = ["/my-country"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMyCountry(request: HttpServletRequest): IpCountryResponse {
        if (ipDatabaseReader == null) {
            return IpCountryResponse(null, null)
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

}
