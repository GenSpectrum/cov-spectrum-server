package ch.ethz.vm.entity.core;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;


public class Variant {

    private final Set<AAMutation> mutations;


    public Variant(Set<AAMutation> mutations) {
        this.mutations = Collections.unmodifiableSet(mutations);
    }


    public Set<AAMutation> getMutations() {
        return mutations;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variant variant = (Variant) o;
        return Objects.equals(mutations, variant.mutations);
    }


    @Override
    public int hashCode() {
        return Objects.hash(mutations);
    }
}
