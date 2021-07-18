package ch.ethz.covspectrum.entity.api;

import ch.ethz.covspectrum.entity.core.Gene;

import java.util.List;

public class ReferenceGenomeResponse {

    private final String nucSeq;
    private final List<Gene> genes;

    public ReferenceGenomeResponse(String nucSeq, List<Gene> genes) {
        this.nucSeq = nucSeq;
        this.genes = genes;
    }

    public String getNucSeq() {
        return nucSeq;
    }

    public List<Gene> getGenes() {
        return genes;
    }
}
