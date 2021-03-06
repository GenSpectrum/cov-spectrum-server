package ch.ethz.covspectrum.entity.api;

public class CasesAndSequences {

    private final int numberCases;

    private final int numberSequenced;


    public CasesAndSequences(int numberCases, int numberSequenced) {
        this.numberCases = numberCases;
        this.numberSequenced = numberSequenced;
    }


    public int getNumberCases() {
        return numberCases;
    }


    public int getNumberSequenced() {
        return numberSequenced;
    }
}
