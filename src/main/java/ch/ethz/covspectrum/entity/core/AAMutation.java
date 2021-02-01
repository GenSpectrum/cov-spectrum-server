package ch.ethz.covspectrum.entity.core;

import java.util.Objects;

public class AAMutation {

    private final String mutationCode;


    /**
     *
     * @param mutationCode The mutation written in our common format. E.g., S:N501Y
     */
    public AAMutation(String mutationCode) {
        this.mutationCode = mutationCode;
    }


    public String getMutationCode() {
        return mutationCode;
    }


    public String getProtein() {
        return mutationCode.split(":")[0];
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AAMutation that = (AAMutation) o;
        return Objects.equals(mutationCode, that.mutationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mutationCode);
    }
}
