package ch.ethz.covspectrum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CovSpectrumServerApplication

fun main(args: Array<String>) {
    runApplication<CovSpectrumServerApplication>(*args)
}
