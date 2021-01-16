package ch.ethz.vm.controller;

import ch.ethz.vm.entity.*;
import ch.ethz.vm.service.DatabaseService;
import ch.ethz.vm.util.BoundedPriorityHeap;
import ch.ethz.vm.util.Counter;
import ch.ethz.vm.util.Utils;
import org.javatuples.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.extra.YearWeek;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/variant")
public class VariantController {

    private final DatabaseService databaseService;


    public VariantController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("")
    public List<VariantStatistics> findNewVariants(
            @RequestParam int year,
            @RequestParam int week,
            @RequestParam String country
    ) throws SQLException {
        YearWeek t1 = YearWeek.of(year, week);
        YearWeek t0 = t1.minusWeeks(1);
        int NUMBER_RESULTS = 200; // The number of variants we want to return

        // Step 1: Find variants in t1 that are not too rare.
        // We will find variants that have between 1 and 30 mutations. For each number of mutations, we will limit
        // ourselves to 1000 different variants.
        int l = 30;
        int k = 1000;
        // <Variant, number samples in t1>
        Map<Integer, List<Pair<Variant, Set<Sample>>>> frequentVariants = new HashMap<>();

        // Fetch mutations
        List<Pair<AAMutation, Set<Sample>>> t1Mutations = databaseService.getMutations(t1, country);
        var tmp = this.findNewVariantsPrepareMutations(t1Mutations);
        Map<AAMutation, Set<Sample>> t1MutationToSample = tmp.getValue0();
        Map<Sample, Set<AAMutation>> t1SampleToMutation = tmp.getValue1();

        // Get top k variants of length 1
        t1Mutations.sort((p1, p2) -> p2.getValue1().size() - p1.getValue1().size());
        List<Pair<Variant, Set<Sample>>> ls = t1Mutations.subList(0, Math.min(t1Mutations.size(), k)).stream()
                .map(p -> new Pair<>(
                        new Variant(new HashSet<>(Collections.singletonList(p.getValue0()))),
                        p.getValue1()
                )).collect(Collectors.toList());
        frequentVariants.put(1, ls);

        // Get top k variants of length >= 2
        for (int i = 2; i <= l; i++) {
            BoundedPriorityHeap<Pair<Variant, Set<Sample>>> ranked = new BoundedPriorityHeap<>(k,
                    Comparator.comparingInt(p -> p.getValue1().size()));
            Set<Variant> known = new HashSet<>();

            // Variants of length i-1 will be extended
            for (Pair<Variant, Set<Sample>> p : frequentVariants.get(i - 1)) {
                Variant variant0 = p.getValue0();
                Set<Sample> samples0 = p.getValue1();

                // If the frequency of the old variant is lower than the lowest in the current top k, stop.
                if (ranked.isFull() && samples0.size() < ranked.peek().getValue1().size()) {
                    break;
                }

                // Find the k most common shared mutation between the samples that are not in variant0.
                Counter<AAMutation> mutationCounter = new Counter<>();
                for (Sample sample : samples0) {
                    Set<AAMutation> mutations = t1SampleToMutation.get(sample);
                    mutations.removeAll(variant0.getMutations());
                    mutationCounter.addAll(mutations);
                }
                List<AAMutation> mostCommon = mutationCounter.getMostCommon(k);

                for (AAMutation mutationToAdd : mostCommon) {
                    // Define new variant
                    Set<AAMutation> variant1Mutations = new HashSet<>(variant0.getMutations());
                    variant1Mutations.add(mutationToAdd);
                    Variant variant1 = new Variant(variant1Mutations);

                    // Nothing to do if the variant was already processed
                    if (known.contains(variant1)) {
                        continue;
                    }
                    known.add(variant1);

                    // If the frequency of the new variant is lower than the lowest in the current top k, stop.
                    Set<Sample> samples1 = new HashSet<>(samples0);
                    samples1.retainAll(t1MutationToSample.get(mutationToAdd));
                    if (ranked.isFull() && samples1.size() < ranked.peek().getValue1().size()) {
                        break;
                    }

                    // Add the new variant as a new candidate for the most frequent variants.
                    ranked.add(new Pair<>(variant1, samples1));
                }
            }
            frequentVariants.put(i, ranked.getSortedList());
        }

        // Merge the frequent variants
        List<Pair<Variant, Integer>> allFrequentVariants = new ArrayList<>();
        for (List<Pair<Variant, Set<Sample>>> list : frequentVariants.values()) {
            for (Pair<Variant, Set<Sample>> p : list) {
                allFrequentVariants.add(new Pair<>(p.getValue0(), p.getValue1().size()));
            }
        }

        // Step 2: Find out the total number of sequences in t0 and t1. This will be used later to compute the
        // proportions.
        int t1TotalCount = databaseService.getNumberSequences(t1, country);
        int t0TotalCount = databaseService.getNumberSequences(t0, country);



        // Step 3: Look up the number of sequences for the found variants in the week before and compare the two weeks.
        // Fetch mutations
        List<Pair<AAMutation, Set<Sample>>> t0Mutations = databaseService.getMutations(t0, country);
        tmp = this.findNewVariantsPrepareMutations(t0Mutations);
        Map<AAMutation, Set<Sample>> t0MutationToSample = tmp.getValue0();

        List<VariantStatistics> allVariantStatistics = new ArrayList<>();
        for (Pair<Variant, Integer> p : allFrequentVariants) {
            Variant variant = p.getValue0();
            int variantCountT1 = p.getValue1();
            double variantProportionT1 = variantCountT1 * 1.0 / t1TotalCount;

            // Find samples in t0 that have all the mutations of the variant.
            List<Set<Sample>> sampleSets = variant.getMutations().stream()
                    .map(t0MutationToSample::get)
                    .collect(Collectors.toList());

            Set<Sample> samplesT0 = Utils.setIntersection(sampleSets);
            int variantCountT0 = samplesT0.size();
            double variantProportionT0 = variantCountT0 * 1.0 / t0TotalCount;

            // Compute statistics
            VariantStatistics variantStatistics = new VariantStatistics(variant, variantCountT0, variantCountT1,
                    variantProportionT0, variantProportionT1);
            allVariantStatistics.add(variantStatistics);
        }

        allVariantStatistics.sort((vs1, vs2) -> {
            // TODO How to use the relative difference? (division by zero problem...)
            double x = vs2.getAbsoluteDifferenceProportion() - vs1.getAbsoluteDifferenceProportion();
            if (x < 0) {
                return -1;
            }
            if (x > 0) {
                return 1;
            }
            return 0;
        });

        // Step 4: Reduce overlap
        // Simple implementation: Keep the supersets of the top X variants
        // TODO Better solution: Use co-occurrence scores
        List<VariantStatistics> finalVariantStatistics = new ArrayList<>();

        for (VariantStatistics vs1 : allVariantStatistics) {
            Set<AAMutation> muts1 = vs1.getVariant().getMutations();
            boolean toIgnore = false;
            List<VariantStatistics> toDelete = new ArrayList<>();
            for (VariantStatistics vs2 : finalVariantStatistics) {
                Set<AAMutation> muts2 = vs2.getVariant().getMutations();
                if (muts2.containsAll(muts1)) {
                    toIgnore = true;
                } else if (muts1.containsAll(muts2)) {
                    if (vs1.getAbsoluteDifferenceProportion() / vs2.getAbsoluteDifferenceProportion() >= 0.8) {
                        toDelete.add(vs2);
                    } else {
                        toIgnore = true;
                    }
                // Keep the better one if they are similar enough (<= 20% difference)
                } else if (Utils.setSymmetricDifference(muts1, muts2).size() * 1.0 /
                        Math.max(muts1.size(), muts2.size()) <= 0.2) {
                    toIgnore = true;
                }
            }
            for (VariantStatistics variantStatistics : toDelete) {
                finalVariantStatistics.remove(variantStatistics);
            }
            if (!toIgnore) {
                finalVariantStatistics.add(vs1);
            }
            if (finalVariantStatistics.size() >= NUMBER_RESULTS) {
                break;
            }
        }

        return finalVariantStatistics;
    }


    private Pair<Map<AAMutation, Set<Sample>>, Map<Sample, Set<AAMutation>>>
    findNewVariantsPrepareMutations(List<Pair<AAMutation, Set<Sample>>> mutations) {
        Map<AAMutation, Set<Sample>> mutationToSample = new HashMap<>();
        Map<Sample, Set<AAMutation>> sampleToMutation = new HashMap<>();
        for (Pair<AAMutation, Set<Sample>> t1Mutation : mutations) {
            AAMutation mutation = t1Mutation.getValue0();
            Set<Sample> samples = t1Mutation.getValue1();
            for (Sample sample : samples) {
                if (!mutationToSample.containsKey(mutation)) {
                    mutationToSample.put(mutation, new HashSet<>());
                }
                if (!sampleToMutation.containsKey(sample)) {
                    sampleToMutation.put(sample, new HashSet<>());
                }
                mutationToSample.get(mutation).add(sample);
                sampleToMutation.get(sample).add(mutation);
            }
        }
        return new Pair<>(mutationToSample, sampleToMutation);
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

}
