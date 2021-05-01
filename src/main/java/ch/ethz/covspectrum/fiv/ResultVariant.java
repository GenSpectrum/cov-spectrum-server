package ch.ethz.covspectrum.fiv;


import ch.ethz.covspectrum.entity.api.ValueWithCI;
import ch.ethz.covspectrum.util.Counter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class ResultVariant {

    private final List<ResultMutation> mutations;

    private final ValueWithCI<Float> a;

    private final ValueWithCI<Float> f;

    private final int absoluteNumberSamplesInPastThreeMonths;

    private final double relativeNumberSamplesInPastThreeMonths;

    @JsonIgnore
    private final Counter<String> pangolinLineages;


    public ResultVariant(
            List<ResultMutation> mutations,
            ValueWithCI<Float> a,
            ValueWithCI<Float> f,
            int absoluteNumberSamplesInPastThreeMonths,
            double relativeNumberSamplesInPastThreeMonths,
            Counter<String> pangolinLineages
    ) {
        this.mutations = mutations;
        this.a = a;
        this.f = f;
        this.absoluteNumberSamplesInPastThreeMonths = absoluteNumberSamplesInPastThreeMonths;
        this.relativeNumberSamplesInPastThreeMonths = relativeNumberSamplesInPastThreeMonths;
        this.pangolinLineages = pangolinLineages;
    }


    public List<ResultMutation> getMutations() {
        return mutations;
    }


    public ValueWithCI<Float> getA() {
        return a;
    }


    public ValueWithCI<Float> getF() {
        return f;
    }


    public int getAbsoluteNumberSamplesInPastThreeMonths() {
        return absoluteNumberSamplesInPastThreeMonths;
    }


    public double getRelativeNumberSamplesInPastThreeMonths() {
        return relativeNumberSamplesInPastThreeMonths;
    }


    public Counter<String> getPangolinLineages() {
        return pangolinLineages;
    }
}
