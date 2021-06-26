package ch.ethz.covspectrum.entity.core;

import java.util.ArrayList;
import java.util.List;

public class PangolinLineageResponse {

    private List<MutationCount> commonMutations = new ArrayList<>();

    private List<MutationCount> commonNucMutations = new ArrayList<>();

    public List<MutationCount> getCommonMutations() {
        return commonMutations;
    }

    public PangolinLineageResponse setCommonMutations(List<MutationCount> commonMutations) {
        this.commonMutations = commonMutations;
        return this;
    }

    public List<MutationCount> getCommonNucMutations() {
        return commonNucMutations;
    }

    public PangolinLineageResponse setCommonNucMutations(List<MutationCount> commonNucMutations) {
        this.commonNucMutations = commonNucMutations;
        return this;
    }
}
