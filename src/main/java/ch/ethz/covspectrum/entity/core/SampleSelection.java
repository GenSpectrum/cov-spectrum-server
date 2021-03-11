package ch.ethz.covspectrum.entity.core;

import org.springframework.lang.Nullable;


public class SampleSelection {

    private final boolean usePrivate;

    @Nullable
    private final Variant variant;

    @Nullable
    private final String country;

    private final float matchPercentage;

    @Nullable
    private final DataType dataType;


    public SampleSelection(
            boolean usePrivate
    ) {
        this(usePrivate, null, null, 0, null);
    }


    public SampleSelection(
            boolean usePrivate,
            DataType dataType
    ) {
        this(usePrivate, null, null, 0, dataType);
    }


    public SampleSelection(
            boolean usePrivate,
            Variant variant,
            float matchPercentage
    ) {
        this(usePrivate, variant, null, matchPercentage, null);
    }


    public SampleSelection(
            boolean usePrivate,
            Variant variant,
            float matchPercentage,
            DataType dataType
    ) {
        this(usePrivate, variant, null, matchPercentage, dataType);
    }


    public SampleSelection(
            boolean usePrivate,
            Variant variant,
            String country,
            float matchPercentage
    ) {
        this(usePrivate, variant, country, matchPercentage, null);
    }


    public SampleSelection(
            boolean usePrivate,
            Variant variant,
            String country,
            float matchPercentage,
            DataType dataType
    ) {
        this.usePrivate = usePrivate;
        this.variant = variant;
        this.country = country;
        this.matchPercentage = matchPercentage;
        this.dataType = dataType;
    }


    public boolean isUsePrivate() {
        return usePrivate;
    }


    public Variant getVariant() {
        return variant;
    }


    public String getCountry() {
        return country;
    }


    public float getMatchPercentage() {
        return matchPercentage;
    }


    @Nullable
    public DataType getDataType() {
        return dataType;
    }
}
