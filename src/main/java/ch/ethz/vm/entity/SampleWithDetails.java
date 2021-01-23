package ch.ethz.vm.entity;

import java.time.LocalDate;
import java.util.List;


public class SampleWithDetails {

    private final String name;

    private final String country;

    private final LocalDate date;

    private final List<AAMutation> mutations;


    public SampleWithDetails(String name, String country, LocalDate date, List<AAMutation> mutations) {
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
