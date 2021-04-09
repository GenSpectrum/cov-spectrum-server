package ch.ethz.covspectrum.fiv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class FindInterestingVariantsTest {

    @Test
    public void testLogisticCurveOptimizer() {
        SimplexLogisticCurveOptimizer optimizer = new SimplexLogisticCurveOptimizer();
        double delta = 0.001;

        List<Integer> t = List.of(0, 1, 2, 3, 4, 5);
        List<Integer> n = List.of(3, 4, 5, 6, 7, 8);
        List<Integer> k = List.of(1, 1, 1, 3, 4, 2);
        double trueMle = 0.08212620374947402;

        double computedMle = optimizer.fit(t, k, n);
        assertEquals(trueMle, computedMle, delta);
    }

}
