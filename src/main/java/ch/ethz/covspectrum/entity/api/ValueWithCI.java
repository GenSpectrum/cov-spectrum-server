package ch.ethz.covspectrum.entity.api;


/**
 * Holds a value and its confidence interval
 */
public class ValueWithCI<T> {

    private final T value;

    private final T ciLower;

    private final T ciUpper;

    private final double confidenceLevel;


    public ValueWithCI(T value, T ciLower, T ciUpper, double confidenceLevel) {
        if (confidenceLevel > 1 || confidenceLevel < 0) {
            throw new IllegalArgumentException("Alpha is not between 0 and 1.");
        }
        this.value = value;
        this.ciLower = ciLower;
        this.ciUpper = ciUpper;
        this.confidenceLevel = confidenceLevel;
    }


    public T getValue() {
        return value;
    }


    public T getCiLower() {
        return ciLower;
    }


    public T getCiUpper() {
        return ciUpper;
    }


    public double getConfidenceLevel() {
        return confidenceLevel;
    }
}
