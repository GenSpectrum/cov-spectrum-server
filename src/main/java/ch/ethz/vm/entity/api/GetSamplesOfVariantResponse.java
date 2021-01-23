package ch.ethz.vm.entity.api;

import ch.ethz.vm.entity.SampleWithDetails;

import java.util.List;


public class GetSamplesOfVariantResponse {

    private final int totalAvailableSamples;

    private final List<SampleWithDetails> samples;


    public GetSamplesOfVariantResponse(int totalAvailableSamples, List<SampleWithDetails> samples) {
        this.totalAvailableSamples = totalAvailableSamples;
        this.samples = samples;
    }


    public int getTotalAvailableSamples() {
        return totalAvailableSamples;
    }


    public List<SampleWithDetails> getSamples() {
        return samples;
    }
}
