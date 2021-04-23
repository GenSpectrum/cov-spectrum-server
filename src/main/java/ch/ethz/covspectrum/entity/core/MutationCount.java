package ch.ethz.covspectrum.entity.core;

public class MutationCount {

    private String mutation;

    private int count;

    private float proportion;

    public String getMutation() {
        return mutation;
    }

    public MutationCount setMutation(String mutation) {
        this.mutation = mutation;
        return this;
    }

    public int getCount() {
        return count;
    }

    public MutationCount setCount(int count) {
        this.count = count;
        return this;
    }

    public float getProportion() {
        return proportion;
    }

    public MutationCount setProportion(float proportion) {
        this.proportion = proportion;
        return this;
    }
}
