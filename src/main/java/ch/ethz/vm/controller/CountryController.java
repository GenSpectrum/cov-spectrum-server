package ch.ethz.vm.controller;

import ch.ethz.vm.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;


@RestController
@RequestMapping("/country")
public class CountryController {

    private final DatabaseService databaseService;


    public CountryController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("")
    public List<String> getAllCountries() throws SQLException {
        return databaseService.getCountryNames();
    }

}
