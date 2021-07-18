package ch.ethz.covspectrum.entity.core;

public class Gene {

    private String name;
    private int startPosition;
    private int endPosition;
    private String aaSeq;

    public String getName() {
        return name;
    }

    public Gene setName(String name) {
        this.name = name;
        return this;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public Gene setStartPosition(int startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public Gene setEndPosition(int endPosition) {
        this.endPosition = endPosition;
        return this;
    }

    public String getAaSeq() {
        return aaSeq;
    }

    public Gene setAaSeq(String aaSeq) {
        this.aaSeq = aaSeq;
        return this;
    }
}
