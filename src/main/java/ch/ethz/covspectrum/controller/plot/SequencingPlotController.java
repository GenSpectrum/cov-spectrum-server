package ch.ethz.covspectrum.controller.plot;

import ch.ethz.covspectrum.entity.api.CasesAndSequences;
import ch.ethz.covspectrum.entity.api.Distribution;
import ch.ethz.covspectrum.entity.core.DataType;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/plot/sequencing")
public class SequencingPlotController {

    private final DatabaseService databaseService;


    public SequencingPlotController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("/time-intensity-distribution")
    public List<Distribution<LocalDate, CasesAndSequences>> getTimeIntensityDistribution(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) DataType dataType
    ) throws SQLException {
        return databaseService.getTimeIntensityDistribution(region, country, dataType);
    }

}
