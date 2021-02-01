package ch.ethz.covspectrum.entity;

import ch.ethz.covspectrum.entity.core.Variant;

import java.util.Objects;

public class VariantStatistics {

    private final Variant variant;

    private final int t0Count;

    private final int t1Count;

    private final double t0Proportion;

    private final double t1Proportion;


    public VariantStatistics(Variant variant, int t0Count, int t1Count, double t0Proportion, double t1Proportion) {
        this.variant = variant;
        this.t0Count = t0Count;
        this.t1Count = t1Count;
        this.t0Proportion = t0Proportion;
        this.t1Proportion = t1Proportion;
    }


    public Variant getVariant() {
        return variant;
    }


    public int getT0Count() {
        return t0Count;
    }


    public int getT1Count() {
        return t1Count;
    }


    public double getT0Proportion() {
        return t0Proportion;
    }


    public double getT1Proportion() {
        return t1Proportion;
    }


    public double getAbsoluteDifferenceProportion() {
        return t1Proportion - t0Proportion;
    }


    public Double getRelativeDifferenceProportion() {
        if (t0Count > 0) {
            return t1Proportion / t0Proportion;
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantStatistics that = (VariantStatistics) o;
        return t0Count == that.t0Count &&
                t1Count == that.t1Count &&
                t0Proportion == that.t0Proportion &&
                t1Proportion == that.t1Proportion &&
                Objects.equals(variant, that.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, t0Count, t1Count, t0Proportion, t1Proportion);
    }
}
