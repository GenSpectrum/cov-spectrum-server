package ch.ethz.vm.controller.plot;

import ch.ethz.vm.entity.*;
import ch.ethz.vm.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/plot/variant")
public class VariantPlotController {

    private final DatabaseService databaseService;


    public VariantPlotController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("/time-distribution")
    public List<DistributionByWeek> getTimeDistribution(
            @RequestParam String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage
    ) throws SQLException {
        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());;
        Variant variant = new Variant(aaMutations);
        return databaseService.getTimeDistribution(variant, country, matchPercentage);
    }


    @GetMapping("/age-distribution")
    public List<DistributionByAgeGroup> getAgeDistribution(
            @RequestParam String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage
    ) throws SQLException {
        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());;
        Variant variant = new Variant(aaMutations);
        return databaseService.getAgeDistribution(variant, country, matchPercentage);
    }


    @GetMapping("/international-time-distribution")
    public List<DistributionByWeekAndCountry> getInternationalTimeDistribution(
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage
    ) throws SQLException {
        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());
        Variant variant = new Variant(aaMutations);
        return databaseService.getInternationalTimeDistribution(variant, matchPercentage);
    }

}
