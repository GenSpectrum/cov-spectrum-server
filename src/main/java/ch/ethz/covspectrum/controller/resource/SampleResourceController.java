package ch.ethz.covspectrum.controller.resource;

import ch.ethz.covspectrum.entity.api.ResultList;
import ch.ethz.covspectrum.entity.core.*;
import ch.ethz.covspectrum.service.DatabaseService;
import ch.ethz.covspectrum.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/resource")
public class SampleResourceController {

    private final DatabaseService databaseService;

    private final ObjectMapper objectMapper;


    public SampleResourceController(DatabaseService databaseService, ObjectMapper objectMapper) {
        this.databaseService = databaseService;
        this.objectMapper = objectMapper;
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
                .collect(Collectors.toSet());
        Variant variant = new Variant(aaMutations);
        List<SampleFull> samples = databaseService.getSamples(variant, country, matchPercentage,
                principal != null, dataType);
        int totalNumber = samples.size();
        if (totalNumber > TOTAL_RETURN_NUMBER) {
            samples = samples.subList(0, TOTAL_RETURN_NUMBER);
        }
        return new ResultList<>(totalNumber, samples);
    }


    @GetMapping(
            value = "/sample2",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getSamples2(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage,
            @RequestParam(required = false) DataType dataType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Principal principal
    ) throws SQLException {
        // Define selection
        Variant variant = null;
        if (mutations != null) {
            Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                    .map(AAMutation::new)
                    .collect(Collectors.toSet());
            variant = new Variant(aaMutations);
        }
        SampleSelection selection = new SampleSelection()
                .setUsePrivate(principal != null).setVariant(variant).setMatchPercentage(matchPercentage)
                .setRegion(region).setCountry(country).setDataType(dataType)
                .setDateFrom(dateFrom).setDateTo(dateTo);

        // Choose fields
        Set<String> ALL_FIELDS = new HashSet<>() {{
            add("date");
            add("region");
            add("country");
            add("division");
            add("zipCode");
            add("ageGroup");
            add("sex");
            add("hospitalized");
            add("deceased");
        }};
        Set<String> fieldsParsed;
        if (fields != null) {
            fieldsParsed = new HashSet<>();
            String[] split = fields.split(",");
            for (String field : split) {
                if (!ALL_FIELDS.contains(field)) {
                    throw new RuntimeException("Unknown field: " + field);
                }
                fieldsParsed.add(field);
            }
        } else {
            fieldsParsed = ALL_FIELDS;
        }

        String result;

        // Check if the value has been pre-computed
        String cached = databaseService.fetchSamplesFromCache(selection, fieldsParsed);
        if (cached != null) {
            result = cached;
        } else {
            // If it's not pre-computed, compute now
            WeightedSampleResultSet samples = databaseService.getSamples2(selection, fieldsParsed);

            // Format results as JSON
            try {
                result = objectMapper.writeValueAsString(samples);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        // Increment the statistics
        databaseService.incrementSampleUsageStatistics(selection, fieldsParsed);

        // Respond
        return result;
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
