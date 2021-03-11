package ch.ethz.covspectrum.entity.core;

import org.springframework.lang.Nullable;


public class SampleSelection {

    private final boolean usePrivate;

    private final Variant variant;

    @Nullable
    private final String country;

    private final float matchPercentage;


    public SampleSelection(
            boolean usePrivate,
            Variant variant,
            float matchPercentage
    ) {
        this.usePrivate = usePrivate;
        this.variant = variant;
        this.country = null;
        this.matchPercentage = matchPercentage;
    }


    public SampleSelection(
            boolean usePrivate,
            Variant variant,
            String country,
            float matchPercentage
    ) {
        this.usePrivate = usePrivate;
        this.variant = variant;
        this.country = country;
        this.matchPercentage = matchPercentage;
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
}
