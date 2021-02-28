package ch.ethz.covspectrum.controller.plot;

import ch.ethz.covspectrum.entity.api.*;
import ch.ethz.covspectrum.entity.core.AAMutation;
import ch.ethz.covspectrum.entity.core.Variant;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.extra.YearWeek;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
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
    public List<Distribution<YearWeek, CountAndProportionWithCI>> getTimeDistribution(
            @RequestParam String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage
    ) throws SQLException {
        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());
        Variant variant = new Variant(aaMutations);
        return databaseService.getWeeklyTimeDistribution(variant, country, matchPercentage);
    }


    @GetMapping("/age-distribution")
    public List<Distribution<String, CountAndProportionWithCI>> getAgeDistribution(
            @RequestParam String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage,
            Principal principal
    ) throws SQLException {
        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());
        Variant variant = new Variant(aaMutations);
        return databaseService.getAgeDistribution(variant, country, matchPercentage, principal != null);
    }


    @GetMapping("/international-time-distribution")
    public List<Distribution<WeekAndCountry, CountAndProportionWithCI>> getInternationalTimeDistribution(
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage
    ) throws SQLException {
        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());
        Variant variant = new Variant(aaMutations);
        return databaseService.getInternationalTimeDistribution(variant, matchPercentage);
    }


    @GetMapping("/time-zip-code-distribution")
    public List<Distribution<WeekAndZipCode, Count>> getTimeZipCodeDistribution(
            @RequestParam String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage,
            Principal principal
    ) throws SQLException {
        // Zip code is only known for Switzerland
        if (!"Switzerland".equals(country)) {
            return new ArrayList<>();
        }

        // All available zip code information is confidential
        if (principal == null) {
            return new ArrayList<>();
        }

        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());
        Variant variant = new Variant(aaMutations);
        return databaseService.getPrivateTimeZipCodeDistributionOfCH(variant, matchPercentage);
    }

}
