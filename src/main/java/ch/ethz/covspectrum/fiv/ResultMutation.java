package ch.ethz.covspectrum.fiv;

public class ResultMutation {

    private final String mutation;

    private final double uniquenessScore;


    public ResultMutation(String mutation, double uniquenessScore) {
        this.mutation = mutation;
        this.uniquenessScore = uniquenessScore;
    }


    public String getMutation() {
        return mutation;
    }


    public double getUniquenessScore() {
        return uniquenessScore;
    }
}
