package ch.ethz.covspectrum

import ch.ethz.covspectrum.service.getDbJdbcUrl
import ch.ethz.covspectrum.service.getDbPassword
import ch.ethz.covspectrum.service.getDbUser
import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [R2dbcAutoConfiguration::class])
class CovSpectrumServerApplication

fun main(args: Array<String>) {
    val flyway = Flyway.configure().dataSource(getDbJdbcUrl(), getDbUser(), getDbPassword()).load()
    flyway.migrate()
    runApplication<CovSpectrumServerApplication>(*args)
}
