package ch.ethz.covspectrum.entity.core;

import java.time.LocalDate;


public class WeightedSample {

    private final LocalDate date;

    private final String region;

    private final String country;

    private final String division;

    private final String zipCode;

    private final String ageGroup;

    private final Boolean hospitalized;

    private final Boolean deceased;

    private final int count;

    public WeightedSample(
            LocalDate date,
            String region,
            String country,
            String division,
            String zipCode,
            String ageGroup,
            Boolean hospitalized,
            Boolean deceased,
            int count
    ) {
        this.date = date;
        this.region = region;
        this.country = country;
        this.division = division;
        this.zipCode = zipCode;
        this.ageGroup = ageGroup;
        this.hospitalized = hospitalized;
        this.deceased = deceased;
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

    public Boolean getHospitalized() {
        return hospitalized;
    }

    public Boolean getDeceased() {
        return deceased;
    }

    public int getCount() {
        return count;
    }
}
