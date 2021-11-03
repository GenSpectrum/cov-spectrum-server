package ch.ethz.covspectrum.controller

import ch.ethz.covspectrum.entity.res.ReferenceGenomeResponse
import ch.ethz.covspectrum.service.DatabaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/resource/reference-genome")
class ReferenceGenomeController(
    databaseService: DatabaseService
) {
    val response: ReferenceGenomeResponse = ReferenceGenomeResponse(
        databaseService.getReferenceGenome(),
        databaseService.getAASeqs()
    );

    @GetMapping("")
    fun getReferenceGenome(): ReferenceGenomeResponse {
        return response;
    }
}
