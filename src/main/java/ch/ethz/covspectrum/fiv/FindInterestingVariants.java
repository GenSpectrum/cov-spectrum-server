package ch.ethz.covspectrum.fiv;

import ch.ethz.covspectrum.entity.core.AAMutation;
import ch.ethz.covspectrum.entity.core.SampleSelection;
import ch.ethz.covspectrum.entity.core.WeightedSample;
import ch.ethz.covspectrum.entity.core.WeightedSampleResultSet;
import ch.ethz.covspectrum.entity.model.chen2021fitness.Response;
import ch.ethz.covspectrum.entity.model.chen2021fitness.WithoutPredictionRequest;
import ch.ethz.covspectrum.service.DatabaseService;
import ch.ethz.covspectrum.util.Counter;
import ch.ethz.covspectrum.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Days;

import java.io.IOException;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class FindInterestingVariants {

    private final Logger logger = LoggerFactory.getLogger(FindInterestingVariants.class);

    private final DatabaseService databaseService;


    public FindInterestingVariants() {
        this.databaseService = new DatabaseService(new ObjectMapper());
    }


    /**
     *
     * @param country The country name
     * @param minNumberSamplesRelative The number of samples that a variant must have in the past three months (relatively)
     * @param maxVariantLength A variant shall only be defined by a limited number of mutations.
     * @param maxNumberOfVariants The maximal number of variants that should be kept
     * @throws SQLException
     */
    public void doWork(
            String country,
            float minNumberSamplesRelative,
            int maxVariantLength,
            int maxNumberOfVariants
    ) throws SQLException, JsonProcessingException {
        // TODO Implement DataType filter?!

        /* Assumptions and settings */
        double GENERATION_TIME = 4.8;
        double MIN_F = 0.05; // We will only keep variants with a fitness advantage of at least f.


        /* Algorithm */

        // Step 1: Fetch all samples and their mutations from the past three months
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);

        // Get the absolute number of minimal required number of samples
        int totalNumberSamples;
        int minNumberSamples;
        try (Connection conn = this.databaseService.getDatabaseConnection()) {
            String sql = """
                select count(*) as count
                from spectrum_sequence_public_meta s
                where
                  s.country = ?
                  and s.date between ? and ?;
            """;
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, country);
                statement.setDate(2, Date.valueOf(startDate));
                statement.setDate(3, Date.valueOf(endDate));
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    totalNumberSamples = rs.getInt("count");
                    minNumberSamples = (int) Math.floor(minNumberSamplesRelative * totalNumberSamples);
                }
            }
        }

        // Fetch samples
        Map<String, List<String>> mutationToSamples = new HashMap<>();
        Map<String, List<String>> sampleToMutations = new HashMap<>();
        try (Connection conn = this.databaseService.getDatabaseConnection()) {
            String sql = """
                select
                  m.aa_mutation,
                  string_agg(s.sequence_name, ',') as samples
                from
                  spectrum_sequence_public_mutation_aa m
                  join spectrum_sequence_public_meta s on m.sequence_name = s.sequence_name
                where
                  s.country = ?
                  and s.date between ? and ?
                group by m.aa_mutation
                having count(*) >= ?;
            """;
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, country);
                statement.setDate(2, Date.valueOf(startDate));
                statement.setDate(3, Date.valueOf(endDate));
                statement.setInt(4, minNumberSamples);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String mutation = rs.getString("aa_mutation");
                        List<String> samples = Arrays.asList(rs.getString("samples").split(","));
                        mutationToSamples.put(mutation, samples);
                        for (String sample : samples) {
                            if (!sampleToMutations.containsKey(sample)) {
                                sampleToMutations.put(sample, new ArrayList<>());
                            }
                            sampleToMutations.get(sample).add(mutation);
                        }
                    }
                }
            }
        }


        // Step 2: Find all variants that are not too rare (>= MIN_NUMBER_SAMPLES)
        // We look at combinations of up to MAX_VARIANT_LENGTH mutations.
        Map<Integer, Set<Variant>> variantsPerLength = new HashMap<>();
        Map<Variant, SampleSet> variantToSamples = new HashMap<>();
        Map<SampleSet, Variant> samplesToVariant = new HashMap<>();
        for (int i = 1; i <= maxVariantLength; i++) {
            variantsPerLength.put(i, new HashSet<>());
        }

        // Variants of length 1 are simply the mutations from above
        variantsPerLength.put(1, new HashSet<>());
        for (String mutation : mutationToSamples.keySet()) {
            Variant variant = new Variant(Collections.singleton(mutation));
            variantsPerLength.get(1).add(variant);
            variantToSamples.put(variant, new SampleSet(mutationToSamples.get(mutation)));
        }

        // From i to i+1: the variants will be extended iteratively
        for (int i = 1; i < maxVariantLength; i++) {
            logger.info("Constructing variants of length " + (i+1) + "...");
            int j = 0;
            Set<Variant> removedVariants = new HashSet<>();
            for (Variant variant_i0 : variantsPerLength.get(i)) {
                if (j++ % 200 == 0) {
                    logger.info("...variant " + j + " of " + variantsPerLength.get(i).size());
                }
                if (removedVariants.contains(variant_i0)) {
                    continue;
                }
                Counter<String> mutationCounter = new Counter<>();
                HashMap<String, List<String>> additionalMutationToSamples = new HashMap<>();
                // For each sample of a given variant...
                for (String sample : variantToSamples.get(variant_i0)) {
                    // ...we loop through all the mutations...
                    for (String additionalMutation : sampleToMutations.get(sample)) {
                        // ...and for mutations that are not already a part of the given variant...
                        if (variant_i0.getMutations().contains(additionalMutation)) {
                            continue;
                        }
                        // ...we will count how often it appears.
                        mutationCounter.add(additionalMutation);
                        if (!additionalMutationToSamples.containsKey(additionalMutation)) {
                            additionalMutationToSamples.put(additionalMutation, new ArrayList<>());
                        }
                        additionalMutationToSamples.get(additionalMutation).add(sample);
                    }
                }
                for (var additionalMutationAndSamples : additionalMutationToSamples.entrySet()) {
                    if (removedVariants.contains(variant_i0)) {
                        break;
                    }
                    String additionalMutation = additionalMutationAndSamples.getKey();
                    if (mutationCounter.getCount(additionalMutation) < minNumberSamples) {
                        continue;
                    }
                    Set<String> mutations_i1 = new HashSet<>(variant_i0.getMutations());
                    mutations_i1.add(additionalMutation);
                    Variant variant_i1 = new Variant(mutations_i1);
                    if (variantToSamples.containsKey(variant_i1) || removedVariants.contains(variant_i1)) {
                        continue;
                    }
                    SampleSet samples_i1 = new SampleSet(additionalMutationAndSamples.getValue());
                    // Merge variants with the exact same sample set
                    if (samplesToVariant.containsKey(samples_i1)) {
                        // Here, we make an exception to the variant length limitation and allow variants to be defined
                        // by more than MAX_VARIANT_LENGTH mutations.
                        Variant existingVariant = samplesToVariant.get(samples_i1);
                        Set<String> mergedMutations = new HashSet<>(existingVariant.getMutations());
                        mergedMutations.addAll(mutations_i1);
                        Variant mergedVariant = new Variant(mergedMutations);
                        int numberMutations = mergedMutations.size();
                        if (numberMutations > existingVariant.getMutations().size()) {
                            if (!variantsPerLength.containsKey(numberMutations)) {
                                variantsPerLength.put(numberMutations, new HashSet<>());
                            }
                            variantsPerLength.get(numberMutations).add(mergedVariant);
                            samplesToVariant.put(samples_i1, mergedVariant);
                            variantToSamples.put(mergedVariant, samples_i1);

                            // Remove redundant variant
                            removedVariants.add(existingVariant);
                            removedVariants.add(variant_i1);
                        }
                    } else {
                        variantsPerLength.get(i + 1).add(variant_i1);
                        variantToSamples.put(variant_i1, samples_i1);
                        samplesToVariant.put(samples_i1, variant_i1);
                    }
                }
            }
            for (Variant removedVariant : removedVariants) {
                variantToSamples.remove(removedVariant);
                variantsPerLength.get(removedVariant.getMutations().size()).remove(removedVariant);
            }
        }


        // Step 3: Compute fitness advantage of the variants. We use f_d because it only depends on the generation
        //         time (which we can safely consider as constant) but not on the reproduction number (which is
        //         varying a lot...).
        variantsPerLength = null;
        samplesToVariant = null;
        logger.info(variantToSamples.size() + " variants were selected. Start estimating their fitness...");

        // Fetch the needed data about the samples
        // The date count starts at "startDate" (t=0)
        Map<String, Integer> sampleToT = new HashMap<>();
        List<Integer> t = new ArrayList<>();
        List<Integer> n = new ArrayList<>();
        for (int i = 0; i < Days.between(startDate, endDate).getAmount() + 1; i++) {
            t.add(i);
            n.add(0);
        }
        try (Connection conn = this.databaseService.getDatabaseConnection()) {
            // Get dates of the samples
            String getSampleDatesSQL = """
                select
                  s.sequence_name as sample,
                  s.date as date
                from
                  spectrum_sequence_public_meta s
                where
                  s.country = ?
                  and s.date between ? and ?;
            """;
            try (PreparedStatement statement = conn.prepareStatement(getSampleDatesSQL)) {
                statement.setString(1, country);
                statement.setDate(2, Date.valueOf(startDate));
                statement.setDate(3, Date.valueOf(endDate));
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        LocalDate date = rs.getDate("date").toLocalDate();
                        int _t = Days.between(startDate, date).getAmount();
                        sampleToT.put(rs.getString("sample"), _t);
                        n.set(_t, n.get(_t) + 1);
                    }
                }
            }
        }

        // We calculate a "uniqueness score" for each mutation defined as the number of samples of a variant divided by
        // the number of total occurrences of that mutation. This can help us to understand if a mutation is unique to
        // the variant or shared by other variants as well.
        Map<String, Integer> mutationToNumberOccurrences = new HashMap<>();
        for (Map.Entry<String, List<String>> mutationAndSamples : mutationToSamples.entrySet()) {
            mutationToNumberOccurrences.put(mutationAndSamples.getKey(), mutationAndSamples.getValue().size());
        }

        // Loop through the selected samples and compute the logistic growth rate
        int failed = 0;
        int total = 0;
        List<ResultVariant> resultVariants = new ArrayList<>();
        for (Map.Entry<Variant, SampleSet> entry : variantToSamples.entrySet()) {
            if (total % 200 == 0) {
                logger.info("...variant " + total + " of " + variantToSamples.size());
            }
            Variant variant = entry.getKey();
            SampleSet samples = entry.getValue();
            int numberSamples = 0;
            List<Integer> k = new ArrayList<>();
            for (int i = 0; i < Days.between(startDate, endDate).getAmount() + 1; i++) {
                k.add(0);
            }
            for (String sample : samples) {
                int _t = sampleToT.get(sample);
                k.set(_t, k.get(_t) + 1);
                numberSamples++;
            }
            final int finalNumberSamples = numberSamples;
            List<ResultMutation> mutations = variant.getMutations().stream()
                    .map(m -> new ResultMutation(m, finalNumberSamples * 1.0 / mutationToNumberOccurrences.get(m)))
                    .collect(Collectors.toList());
            try {
                Response.Params growthParams = estimateGrowth(new WithoutPredictionRequest(
                        new WithoutPredictionRequest.InnerData(t, n, k),
                        0.95f, 4.8f, 1
                ));
                resultVariants.add(new ResultVariant(
                        mutations,
                        growthParams.getA().toGeneralValueWithCI(0.95),
                        growthParams.getFd().toGeneralValueWithCI(0.95),
                        numberSamples,
                        numberSamples * 1.0 / totalNumberSamples
                ));
            } catch (Exception e) {
                failed++;
            } finally {
                total++;
            }
        }
        logger.info("Fitness advantage estimation failed for " + failed + " out of " + total + ".");

        // We will only keep variants where the lower bound of the CI of f >= 0.05.
        resultVariants = resultVariants.stream()
                .filter(v -> v.getF().getCiLower() >= MIN_F)
                .collect(Collectors.toUnmodifiableList());


        // Step 4:  Compare the fitness advantage of the found variants with those of "known" variants. We use
        //          pangolin lineages as reference variants.
        logger.info("Start comparing the fitness advantage of " + resultVariants.size() +
                " variants with known pangolin lineages...");

        // Fetch the pre-computed fitness advantage values of pangolin lineages that show an increased transmissibility.
        Map<String, Float> pangolinLineageFitnessAdvantages = new HashMap<>();
        String fetchFitnessAdvantageOfLineagesSql = """
            select
              plm.pangolin_lineage,
              plm.fitness_advantage
            from
              (
                select
                  row_number() over (partition by plm.pangolin_lineage order by insertion_timestamp desc) as priority_idx,
                  *
                from spectrum_pangolin_lineage_recent_metrics plm
                where
                  country = ?
              ) plm
            where
              plm.priority_idx = 1
              and plm.fitness_advantage_lower >= 0.05;
        """;
        try (Connection conn = this.databaseService.getDatabaseConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(fetchFitnessAdvantageOfLineagesSql)) {
                statement.setString(1, country);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        pangolinLineageFitnessAdvantages.put(
                                rs.getString("pangolin_lineage"),
                                rs.getFloat("fitness_advantage")
                        );
                    }
                }
            }
        }

        // Find out the pangolin lineages of the variants
        List<ResultVariant> resultVariants2 = new ArrayList<>();
        int j = 0;
        for (ResultVariant variant : resultVariants) {
            if (j++ % 200 == 0) {
                logger.info("...variant " + j + " of " + resultVariants.size());
            }
            // TODO This is not efficient. We are already loading all relevant samples earlier and the pangolin
            //    lineages can be loaded at the same time.
            WeightedSampleResultSet lineages = databaseService.getSamples2(
                    new SampleSelection()
                            .setCountry(country)
                            .setDateFrom(startDate)
                            .setDateTo(endDate)
                            .setMatchPercentage(1)
                            .setVariant(new ch.ethz.covspectrum.entity.core.Variant(
                                    variant.getMutations().stream()
                                            .map(m -> new AAMutation(m.getMutation()))
                                            .collect(Collectors.toSet())
                            )),
                    Collections.singletonList("pangolinLineage")
            );

            // Step 4a: If the majority of the sequences of the variant belong to the same pangolin lineage,
            //          the variant shall be compared against that lineage. The variant will only be kept if
            //          it has a significant higher transmission advantage than the known pangolin lineage.
            //          We consider the f of the variant as "significant higher" if the lower bound of the CI
            //          of the f of the variant is larger than the point estimate of the f of the pangolin lineage.
            int totalSamples = lineages.getWeightedSamples().stream()
                    .mapToInt(WeightedSample::getCount)
                    .sum();
            int majorityThreshold = (int) Math.floor(totalSamples * 0.9);
            Optional<WeightedSample> mainLineageOpt = lineages.getWeightedSamples().stream()
                    .filter(l -> l.getCount() >= majorityThreshold)
                    .findAny();
            if (mainLineageOpt.isPresent()) {
                String mainLineage = mainLineageOpt.get().getPangolinLineage();
                float minF = pangolinLineageFitnessAdvantages.getOrDefault(mainLineage, 0.05f);
                if (variant.getF().getCiLower() > minF) {
                    resultVariants2.add(variant);
                }
            }

            // Step 4b: If the variant cannot be matched to a single pangolin lineage, we compare the f of
            //          the variant with the unweighted mean of the fitness advantage values of all lineages
            //          that contain at least 5% of the sequences of the variant.
            else {
                int fivePercentThreshold = (int) Math.floor(totalSamples * 0.05);
                List<String> relatedLineages = lineages.getWeightedSamples().stream()
                        .filter(l -> l.getCount() >= fivePercentThreshold)
                        .map(WeightedSample::getPangolinLineage)
                        .collect(Collectors.toList());
                double minF = relatedLineages.stream()
                        .mapToDouble(l -> pangolinLineageFitnessAdvantages.getOrDefault(l, 0.05f))
                        .average()
                        .orElse(0.05);
                if (variant.getF().getCiLower() > minF) {
                    resultVariants2.add(variant);
                }
            }
        }

        // We will only keep the top maxNumberOfVariants variants.
        resultVariants2 = resultVariants2.stream()
                .sorted(Comparator.comparingDouble((ResultVariant v) -> v.getF().getValue()).reversed())
                .limit(maxNumberOfVariants)
                .collect(Collectors.toUnmodifiableList());


        // Step 5: Save to database
        // Create the final result object
        Result result = new Result(LocalDate.now(), resultVariants2);

        // Format results as JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json = objectMapper.writeValueAsString(result);

        // Write..
        logger.info("Start writing the results (" + resultVariants2.size() + " variants) into the database.");
        Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
        try (Connection conn = this.databaseService.getDatabaseConnection()) {
            String sql = """
                insert into spectrum_new_interesting_variant (insertion_timestamp, country, result)
                values (?, ?, ?);
            """;
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setTimestamp(1, currentTimestamp);
                statement.setString(2, country);
                statement.setString(3, json);
                statement.execute();
            }
        }
    }


    private Response.Params estimateGrowth(WithoutPredictionRequest request) throws IOException {
        String ENDPOINT = "http://localhost:7070/without-prediction";
        String json = new ObjectMapper().writeValueAsString(request);
        String responseString = Utils.postRequest(ENDPOINT, json);
        return new ObjectMapper().readValue(responseString, Response.Params.class);
    }

}
