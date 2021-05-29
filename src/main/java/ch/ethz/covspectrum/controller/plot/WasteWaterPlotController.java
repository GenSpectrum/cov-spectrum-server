package ch.ethz.covspectrum.controller.plot;

import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;


@RestController
@RequestMapping("/plot/waste-water")
public class WasteWaterPlotController {

    private final DatabaseService databaseService;


    public WasteWaterPlotController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GetMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getWasteWaterResults(
            @RequestParam String country
    ) throws SQLException {
        return databaseService.getWasteWaterResults(country);
    }

}
