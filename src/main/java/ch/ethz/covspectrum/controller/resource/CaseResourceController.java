package ch.ethz.covspectrum.controller.resource;

import ch.ethz.covspectrum.entity.core.CaseCounts;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/resource/case")
public class CaseResourceController {

    private final DatabaseService databaseService;


    public CaseResourceController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GetMapping("")
    public List<CaseCounts> getTimeIntensityDistribution(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "false") boolean includeDate
    ) throws SQLException {
        // We only have detailed case data for Switzerland.
        if (!"Switzerland".equals(country) || (region != null && !"Europe".equals(region))) {
            return null;
        }

        System.out.println(includeDate);

        return databaseService.getSwissCaseCounts(dateFrom, dateTo, includeDate);
    }

}
