package ch.ethz.covspectrum.controller;

import ch.ethz.covspectrum.entity.api.DataStatus;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;


@RestController
public class GeneralController {

    private final DatabaseService databaseService;


    public GeneralController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("/data-status")
    public DataStatus getDataStatus() throws SQLException {
        return this.databaseService.getDataStatus();
    }

}
