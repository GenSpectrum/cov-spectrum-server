package ch.ethz.covspectrum.entity.core;

import java.time.LocalDate;


public class WeightedSample {

    private final LocalDate date;

    private final String region;

    private final String country;

    private final String division;

    private final String zipCode;

    private final String ageGroup;

    private final String sex;

    private final Boolean hospitalized;

    private final Boolean deceased;

    private final String pangolinLineage;

    private final String submittingLab;

    private final int count;

    public WeightedSample(
            LocalDate date,
            String region,
            String country,
            String division,
            String zipCode,
            String ageGroup,
            String sex,
            Boolean hospitalized,
            Boolean deceased,
            String pangolinLineage,
            String submittingLab,
            int count
    ) {
        this.date = date;
        this.region = region;
        this.country = country;
        this.division = division;
        this.zipCode = zipCode;
        this.ageGroup = ageGroup;
        this.sex = sex;
        this.hospitalized = hospitalized;
        this.deceased = deceased;
        this.pangolinLineage = pangolinLineage;
        this.submittingLab = submittingLab;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public String getDivision() {
        return division;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public String getSex() {
        return sex;
    }

    public Boolean getHospitalized() {
        return hospitalized;
    }

    public Boolean getDeceased() {
        return deceased;
    }

    public String getPangolinLineage() {
        return pangolinLineage;
    }

    public String getSubmittingLab() {
        return submittingLab;
    }

    public int getCount() {
        return count;
    }
}
