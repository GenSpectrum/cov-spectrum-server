package ch.ethz.covspectrum.entity.core;

import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;


public class SampleFull {

    private final String name;

    private final String country;

    private final LocalDate date;

    private final List<AAMutation> mutations;

    @Nullable
    private final SamplePrivateMetadata metadata;


    public SampleFull(
            String name,
            String country,
            LocalDate date,
            List<AAMutation> mutations,
            @Nullable SamplePrivateMetadata privateMetadata
    ) {
        this.name = name;
        this.country = country;
        this.date = date;
        this.mutations = mutations;
        this.metadata = privateMetadata;
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


    @Nullable
    public SamplePrivateMetadata getMetadata() {
        return metadata;
    }
}
