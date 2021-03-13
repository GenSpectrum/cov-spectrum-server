package ch.ethz.covspectrum.fiv;


public class IdentifiedVariant {

    private final Variant variant;

    private final double a;

    private final double f;

    private final int absoluteNumberSamplesInPastThreeMonths;

    private final double relativeNumberSamplesInPastThreeMonths;


    public IdentifiedVariant(
            Variant variant,
            double a,
            double f,
            int absoluteNumberSamplesInPastThreeMonths,
            double relativeNumberSamplesInPastThreeMonths
    ) {
        this.variant = variant;
        this.a = a;
        this.f = f;
        this.absoluteNumberSamplesInPastThreeMonths = absoluteNumberSamplesInPastThreeMonths;
        this.relativeNumberSamplesInPastThreeMonths = relativeNumberSamplesInPastThreeMonths;
    }


    public Variant getVariant() {
        return variant;
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
