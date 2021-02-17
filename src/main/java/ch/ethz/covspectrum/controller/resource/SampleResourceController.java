package ch.ethz.covspectrum.controller.resource;

import ch.ethz.covspectrum.entity.core.AAMutation;
import ch.ethz.covspectrum.entity.core.SampleFull;
import ch.ethz.covspectrum.entity.core.Variant;
import ch.ethz.covspectrum.entity.api.ResultList;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/resource/sample")
public class SampleResourceController {

    private final DatabaseService databaseService;


    public SampleResourceController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("")
    public ResultList<SampleFull> getSamples(
            @RequestParam(required = false) String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage,
            Principal principal
    ) throws SQLException {
        int TOTAL_RETURN_NUMBER = 1000;  // I don't want to return too much right now...

        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());;
        Variant variant = new Variant(aaMutations);
        List<SampleFull> samples = databaseService.getSamples(variant, matchPercentage,
                principal != null);
        if (country != null) {
            samples = samples.stream().filter(s -> country.equals(s.getCountry())).collect(Collectors.toList());
        }
        int totalNumber = samples.size();
        if (totalNumber > TOTAL_RETURN_NUMBER) {
            samples = samples.subList(0, TOTAL_RETURN_NUMBER);
        }
        return new ResultList<>(totalNumber, samples);
    }

}
