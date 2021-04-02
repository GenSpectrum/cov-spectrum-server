package ch.ethz.covspectrum.fiv;

import java.time.LocalDate;
import java.util.List;


public class Result {

    private final LocalDate computedAt;

    private final List<ResultVariant> variants;

    public Result(LocalDate computedAt, List<ResultVariant> variants) {
        this.computedAt = computedAt;
        this.variants = variants;
    }


    public LocalDate getComputedAt() {
        return computedAt;
    }

    public List<ResultVariant> getVariants() {
        return variants;
    }
}
