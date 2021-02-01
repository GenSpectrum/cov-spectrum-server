package ch.ethz.covspectrum.entity.core;

import java.util.Objects;


public class SampleName {

    private final String name;


    public SampleName(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleName sample = (SampleName) o;
        return Objects.equals(name, sample.name);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
