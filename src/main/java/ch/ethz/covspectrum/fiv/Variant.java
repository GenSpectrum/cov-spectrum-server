package ch.ethz.covspectrum.fiv;

import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;


public class Variant {

    @Nullable
    private final String name;

    private final Set<String> mutations;


    public Variant(Set<String> mutations) {
        this(null, mutations);
    }


    public Variant(String name, Set<String> mutations) {
        this.name = name;
        this.mutations = Collections.unmodifiableSet(mutations);
    }


    @Nullable
    public String getName() {
        return name;
    }


    public Set<String> getMutations() {
        return mutations;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variant variant = (Variant) o;
        return Objects.equals(name, variant.name) && mutations.equals(variant.mutations);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, mutations);
    }
}
