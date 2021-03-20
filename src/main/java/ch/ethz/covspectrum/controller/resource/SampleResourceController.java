package ch.ethz.covspectrum.controller.resource;

import ch.ethz.covspectrum.entity.core.*;
import ch.ethz.covspectrum.entity.api.ResultList;
import ch.ethz.covspectrum.service.DatabaseService;
import ch.ethz.covspectrum.util.Utils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/resource")
public class SampleResourceController {

    private final DatabaseService databaseService;


    public SampleResourceController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("/sample")
    public ResultList<SampleFull> getSamples(
            @RequestParam(required = false) String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage,
            @RequestParam(required = false) DataType dataType,
            Principal principal
    ) throws SQLException {
        int TOTAL_RETURN_NUMBER = 1000;  // I don't want to return too much right now...

        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());;
        Variant variant = new Variant(aaMutations);
        List<SampleFull> samples = databaseService.getSamples(variant, country, matchPercentage,
                principal != null, dataType);
        int totalNumber = samples.size();
        if (totalNumber > TOTAL_RETURN_NUMBER) {
            samples = samples.subList(0, TOTAL_RETURN_NUMBER);
        }
        return new ResultList<>(totalNumber, samples);
    }


    @GetMapping("/sample2")
    public List<WeightedSample> getSamples2(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage,
            @RequestParam(required = false) DataType dataType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Principal principal
    ) throws SQLException {
        return databaseService.getSamples2(
                region,
                country,
                mutations,
                matchPercentage,
                dataType,
                dateFrom,
                dateTo,
                principal != null
        );
    }


    @GetMapping("/sample-fasta")
    public String getFasta(
            @RequestParam(required = false) String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage,
            @RequestParam(required = false) DataType dataType,
            Principal principal
    ) throws SQLException {
        List<SampleFull> samples = this.getSamples(country, mutations, matchPercentage, dataType, principal).getData();
        List<SampleName> names = samples.stream()
                .map(s -> new SampleName(s.getName()))
                .collect(Collectors.toList());
        List<SampleSequence> sequences = databaseService.getSampleSequences(names, principal != null);
        return Utils.toFasta(sequences);
    }

}
