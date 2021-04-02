package ch.ethz.covspectrum.fiv;


import java.util.List;

public class ResultVariant {

    private final List<ResultMutation> mutations;

    private final double a;

    private final double f;

    private final int absoluteNumberSamplesInPastThreeMonths;

    private final double relativeNumberSamplesInPastThreeMonths;


    public ResultVariant(
            List<ResultMutation> mutations,
            double a,
            double f,
            int absoluteNumberSamplesInPastThreeMonths,
            double relativeNumberSamplesInPastThreeMonths
    ) {
        this.mutations = mutations;
        this.a = a;
        this.f = f;
        this.absoluteNumberSamplesInPastThreeMonths = absoluteNumberSamplesInPastThreeMonths;
        this.relativeNumberSamplesInPastThreeMonths = relativeNumberSamplesInPastThreeMonths;
    }


    public List<ResultMutation> getMutations() {
        return mutations;
    }


    public double getA() {
        return a;
    }


    public double getF() {
        return f;
    }


    public int getAbsoluteNumberSamplesInPastThreeMonths() {
        return absoluteNumberSamplesInPastThreeMonths;
    }


    public double getRelativeNumberSamplesInPastThreeMonths() {
        return relativeNumberSamplesInPastThreeMonths;
    }
}
