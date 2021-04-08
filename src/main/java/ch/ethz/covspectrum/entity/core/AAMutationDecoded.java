package ch.ethz.covspectrum.entity.core;

import org.springframework.lang.Nullable;

public class AAMutationDecoded {

    private final String gene;

    private final int position;

    @Nullable
    private final String base;


    public AAMutationDecoded(String gene, int position, String base) {
        this.gene = gene;
        this.position = position;
        this.base = base;
    }


    public String getGene() {
        return gene;
    }


    public int getPosition() {
        return position;
    }


    public String getBase() {
        return base;
    }


    @Override
    public String toString() {
        return gene.toLowerCase() + ":" + position + (base != null ? base.toLowerCase() : "");
    }
}
