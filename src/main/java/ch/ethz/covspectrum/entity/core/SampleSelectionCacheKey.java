package ch.ethz.covspectrum.entity.core;

import org.javatuples.Pair;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SampleSelectionCacheKey {

    private final String fields;

    private final boolean privateVersion;

    private final String region;

    private final String country;

    private final String mutations;

    private final float matchPercentage;

    private final String dataType;

    private final LocalDate dateFrom;

    private final LocalDate dateTo;


    public SampleSelectionCacheKey(
            String fields,
            boolean privateVersion,
            String region,
            String country,
            String mutations,
            float matchPercentage,
            String dataType,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        this.fields = fields;
        this.privateVersion = privateVersion;
        this.region = region;
        this.country = country;
        this.mutations = mutations;
        this.matchPercentage = matchPercentage;
        this.dataType = dataType;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }


    public String getFields() {
        return fields;
    }


    public boolean isPrivateVersion() {
        return privateVersion;
    }


    public String getRegion() {
        return region;
    }


    public String getCountry() {
        return country;
    }


    public String getMutations() {
        return mutations;
    }


    public float getMatchPercentage() {
        return matchPercentage;
    }


    public String getDataType() {
        return dataType;
    }


    public LocalDate getDateFrom() {
        return dateFrom;
    }


    public LocalDate getDateTo() {
        return dateTo;
    }


    public static SampleSelectionCacheKey fromSampleSelection(SampleSelection selection, Collection<String> fields) {
        String mutationsString = "";
        if (selection.getVariant() != null) {
            mutationsString = selection.getVariant().getMutations().stream()
                    .map(AAMutation::decode)
                    .map(AAMutationDecoded::toString)
                    .sorted()
                    .collect(Collectors.joining(","));
        }
        String dataType = "";
        if (selection.getDataType() != null) {
            dataType = selection.getDataType().toString();
        }
        return new SampleSelectionCacheKey(
                fields.stream().sorted().collect(Collectors.joining(",")),
                selection.isUsePrivate(),
                Objects.requireNonNullElse(selection.getRegion(), ""),
                Objects.requireNonNullElse(selection.getCountry(), ""),
                mutationsString,
                selection.getMatchPercentage(),
                dataType,
                Objects.requireNonNullElse(selection.getDateFrom(), LocalDate.of(1990, 1, 1)),
                Objects.requireNonNullElse(selection.getDateTo(), LocalDate.of(1990, 1, 1))
        );
    }


    /**
     *
     * @return (sample selection, fields)
     */
    public Pair<SampleSelection, List<String>> toSampleSelection() {
        SampleSelection selection = new SampleSelection()
                .setUsePrivate(privateVersion)
                .setRegion(!region.equals("") ? region : null)
                .setCountry(!country.equals("") ? country : null)
                .setMatchPercentage(matchPercentage)
                .setDateFrom(!dateFrom.equals(LocalDate.of(1990, 1, 1)) ? dateFrom : null)
                .setDateTo(!dateTo.equals(LocalDate.of(1990, 1, 1)) ? dateTo : null);
        if (!mutations.equals("")) {
            selection.setVariant(new Variant(Arrays.stream(mutations.split(","))
                    .map(AAMutation::new)
                    .collect(Collectors.toSet())));
        }
        if (!dataType.equals("")) {
            selection.setDataType(DataType.valueOf(dataType));
        }
        return new Pair<>(selection, Arrays.stream(fields.split(",")).collect(Collectors.toList()));
    }
}
