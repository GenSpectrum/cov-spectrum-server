package ch.ethz.covspectrum.entity.core;

import java.time.LocalDate;


public class SampleSelection {

    private boolean usePrivate = false;

    private Variant variant;

    private String pangolinLineage;

    private String region;

    private String country;

    private float matchPercentage = 1;

    private DataType dataType;

    private LocalDate dateFrom;

    private LocalDate dateTo;


    public boolean isUsePrivate() {
        return usePrivate;
    }

    public SampleSelection setUsePrivate(boolean usePrivate) {
        this.usePrivate = usePrivate;
        return this;
    }

    public Variant getVariant() {
        return variant;
    }

    public SampleSelection setVariant(Variant variant) {
        this.variant = variant;
        return this;
    }

    public String getPangolinLineage() {
        return pangolinLineage;
    }

    public SampleSelection setPangolinLineage(String pangolinLineage) {
        this.pangolinLineage = pangolinLineage;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public SampleSelection setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public SampleSelection setCountry(String country) {
        this.country = country;
        return this;
    }

    public float getMatchPercentage() {
        return matchPercentage;
    }

    public SampleSelection setMatchPercentage(float matchPercentage) {
        this.matchPercentage = matchPercentage;
        return this;
    }

    public DataType getDataType() {
        return dataType;
    }

    public SampleSelection setDataType(DataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public SampleSelection setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public SampleSelection setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
        return this;
    }
}
