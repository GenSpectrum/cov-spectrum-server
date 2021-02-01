package ch.ethz.covspectrum.entity.core;

import java.time.LocalDate;
import java.util.List;


public class SampleFull {

    private final String name;

    private final String country;

    private final LocalDate date;

    private final List<AAMutation> mutations;


    public SampleFull(String name, String country, LocalDate date, List<AAMutation> mutations) {
        this.name = name;
        this.country = country;
        this.date = date;
        this.mutations = mutations;
    }


    public String getName() {
        return name;
    }


    public String getCountry() {
        return country;
    }


    public LocalDate getDate() {
        return date;
    }


    public List<AAMutation> getMutations() {
        return mutations;
    }
}
