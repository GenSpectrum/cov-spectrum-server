package ch.ethz.covspectrum.entity.core;

public class SampleSequence {

    private final String sampleName;

    private final String sampleSequence;


    public SampleSequence(String sampleName, String sampleSequence) {
        this.sampleName = sampleName;
        this.sampleSequence = sampleSequence;
    }


    public String getSampleName() {
        return sampleName;
    }


    public String getSampleSequence() {
        return sampleSequence;
    }
}
