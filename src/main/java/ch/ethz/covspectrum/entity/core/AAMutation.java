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


    public AAMutationDecoded decode() {
        String[] split = mutationCode.split(":");
        String gene = split[0];
        String withinGeneMutation = split[1];

        // Remove the original base if provided
        if (!Character.isDigit(withinGeneMutation.charAt(0))) {
            withinGeneMutation = withinGeneMutation.substring(1);
        }

        // The new mutated base might be provided (S:501Y) or not (S:501)
        int position;
        String base = null;
        if (!Character.isDigit(withinGeneMutation.charAt(withinGeneMutation.length() - 1))) {
            position = Integer.parseInt(withinGeneMutation.substring(0, withinGeneMutation.length() - 1));
            base = String.valueOf(withinGeneMutation.charAt(withinGeneMutation.length() - 1));
        } else {
            position = Integer.parseInt(withinGeneMutation);
        }

        return new AAMutationDecoded(gene, position, base);
    }
}
