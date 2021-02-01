package ch.ethz.covspectrum.entity.api;

import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.apache.commons.math3.stat.interval.IntervalUtils;

public class CountAndProportionWithCI {

    private final int count;

    private final int total;

    private final ValueWithCI<Double> proportion;


    public CountAndProportionWithCI(int count, int total, ValueWithCI<Double> proportion) {
        this.count = count;
        this.total = total;
        this.proportion = proportion;
    }


    public int getCount() {
        return count;
    }


    public int getTotal() {
        return total;
    }


    public ValueWithCI<Double> getProportion() {
        return proportion;
    }


    /**
     * Uses confidenceLevel=0.95
     */
    public static CountAndProportionWithCI fromWilsonCI(int count, int total) {
        return CountAndProportionWithCI.fromWilsonCI(count, total, 0.95);
    }


    public static CountAndProportionWithCI fromWilsonCI(int count, int total, double confidenceLevel) {
        ConfidenceInterval ci = IntervalUtils.getWilsonScoreInterval(total, count, confidenceLevel);
        ValueWithCI<Double> proportion = new ValueWithCI<>(
                count * 1.0 / total,
                ci.getLowerBound(),
                ci.getUpperBound(),
                confidenceLevel
        );
        return new CountAndProportionWithCI(count, total, proportion);
    }
}
