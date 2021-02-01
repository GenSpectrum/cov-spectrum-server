package ch.ethz.covspectrum.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utils {


    public static <T> Set<T> setIntersection(Set<T> set1, Set<T> set2) {
        ArrayList<Set<T>> sets = new ArrayList<>() {{
            add(set1);
            add(set2);
        }};
        return Utils.setIntersection(sets);
    }


    public static <T> Set<T> setIntersection(Iterable<Set<T>> sets) {
        Set<T> result = null;
        for (Set<T> set : sets) {
            if (set == null) {
                continue;
            }
            if (result == null) {
                result = new HashSet<>(set);
            } else {
                result.retainAll(set);
            }
        }
        if (result == null) {
            result = new HashSet<>();
        }
        return result;
    }


    public static <T> Set<T> setSymmetricDifference(Set<T> set1, Set<T> set2) {
        Set<T> intersection = setIntersection(Arrays.asList(set1, set2));
        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);
        union.removeAll(intersection);
        return union;
    }
}
