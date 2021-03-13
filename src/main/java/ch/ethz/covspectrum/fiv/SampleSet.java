package ch.ethz.covspectrum.fiv;

import java.util.*;
import java.util.function.Consumer;


/**
 * The is a simple wrapper for a unmodifiable string list that caches its hash code.
 */
public class SampleSet implements Iterable<String> {

    private final List<String> inner;

    private boolean hashed = false;

    private int hash;

    public SampleSet(List<String> inner) {
        Collections.sort(inner);
        this.inner = Collections.unmodifiableList(inner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleSet sampleSet = (SampleSet) o;
        return Objects.equals(inner, sampleSet.inner);
    }

    @Override
    public int hashCode() {
        if (!hashed) {
            hash = inner.hashCode();
            hashed = true;
        }
        return hash;
    }

    @Override
    public Iterator<String> iterator() {
        return inner.iterator();
    }

    @Override
    public Spliterator<String> spliterator() {
        return inner.spliterator();
    }

    public void forEach(Consumer<? super String> action) {
        inner.forEach(action);
    }
}
