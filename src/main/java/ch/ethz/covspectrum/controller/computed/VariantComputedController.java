package ch.ethz.covspectrum.controller.computed;

import ch.ethz.covspectrum.entity.core.DataType;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;


@RestController
@RequestMapping("/computed")
public class VariantComputedController {


    private final DatabaseService databaseService;


    public VariantComputedController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping(
            value="/find-interesting-variants",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String findInterestingVariants(
            @RequestParam String country,
            @RequestParam(required = false) DataType dataType
    ) throws SQLException {
        String result = databaseService.getPrecomputedInterestingVariants(country, dataType);
        if (result == null) {
            return "{\"computedAt\": \"2020-01-01\", \"variants\": []}";
        }
        return result;
    }

}
