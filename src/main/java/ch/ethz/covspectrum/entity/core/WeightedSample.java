package ch.ethz.covspectrum.entity.core;

import java.time.LocalDate;


public class WeightedSample {

    private final LocalDate date;

    private final String region;

    private final String country;

    private final String division;

    private final String zip_code;

    private final String age_group;

    private final Boolean hospitalized;

    private final Boolean deceased;

    private final int count;

    public WeightedSample(
            LocalDate date,
            String region,
            String country,
            String division,
            String zip_code,
            String age_group,
            Boolean hospitalized,
            Boolean deceased,
            int count
    ) {
        this.date = date;
        this.region = region;
        this.country = country;
        this.division = division;
        this.zip_code = zip_code;
        this.age_group = age_group;
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

    public String getZip_code() {
        return zip_code;
    }

    public String getAge_group() {
        return age_group;
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
