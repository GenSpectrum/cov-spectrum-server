package ch.ethz.covspectrum.util;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Counter<T> {

    private final Map<T, Integer> counts = new HashMap<>();


    public void add(T t) {
        counts.merge(t, 1, Integer::sum);
    }


    public void addAll(Iterable<T> ts) {
        for (T t : ts) {
            add(t);
        }
    }


    public int getCount(T t) {
        return counts.getOrDefault(t, 0);
    }


    /**
     *
     * @return A list with the most common at the beginning
     */
    public List<Pair<T, Integer>> getSorted() {
        List<Pair<T, Integer>> result = new ArrayList<>();
        for (Map.Entry<T, Integer> tIntegerEntry : counts.entrySet()) {
            result.add(new Pair<>(tIntegerEntry.getKey(), tIntegerEntry.getValue()));
        }
        result.sort((p1, p2) -> p2.getValue1() - p1.getValue1());
        return result;
    }


    public List<T> getMostCommon(int k) {
        List<T> result = getSorted().stream()
                .map(Pair::getValue0)
                .collect(Collectors.toList());
        return result.subList(0, Math.min(k, result.size()));
    }


    public int getTotalCount() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }
}
