package ch.ethz.covspectrum.controller.plot;

import ch.ethz.covspectrum.entity.api.CasesAndSequences;
import ch.ethz.covspectrum.entity.api.Distribution;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.extra.YearWeek;

import java.sql.SQLException;
import java.util.List;


@RestController
@RequestMapping("/plot/sequencing")
public class SequencingPlotController {

    private final DatabaseService databaseService;


    public SequencingPlotController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("/time-intensity-distribution")
    public List<Distribution<YearWeek, CasesAndSequences>> getTimeIntensityDistribution(
            @RequestParam String country
    ) throws SQLException {
        return databaseService.getTimeIntensityDistribution(country);
    }

}
