package ch.ethz.covspectrum.entity.core;

import java.util.List;


public class WeightedSampleResultSet {

    private final List<String> fields;

    private final List<WeightedSample> weightedSamples;


    public WeightedSampleResultSet(List<String> fields,  List<WeightedSample> weightedSamples) {
        this.fields = fields;
        this.weightedSamples = weightedSamples;
    }


    public List<String> getFields() {
        return fields;
    }


    public List<WeightedSample> getWeightedSamples() {
        return weightedSamples;
    }

}
