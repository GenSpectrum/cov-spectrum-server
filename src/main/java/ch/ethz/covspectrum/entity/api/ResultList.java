package ch.ethz.covspectrum.entity.api;

import java.util.List;


/**
 * Objects of this class are designed to hold result entries instead of a simple List whenever it is possible that not
 * all available results will be returned.
 */
public class ResultList<T> {

    /**
     * The total number of available results. It might be larger than data.size().
     */
    private final int total;

    private final List<T> data;


    public ResultList(int total, List<T> data) {
        this.total = total;
        this.data = data;
    }


    public int getTotal() {
        return total;
    }


    public List<T> getData() {
        return data;
    }
}
