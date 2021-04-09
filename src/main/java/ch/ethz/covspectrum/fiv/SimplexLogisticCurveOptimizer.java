package ch.ethz.covspectrum.fiv;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SimplexLogisticCurveOptimizer {

    public double fit(List<Integer> t, List<Integer> k, List<Integer> n) {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final Objective objective = new Objective(t, k, n);

        final PointValuePair optimum = optimizer.optimize(
                new MaxEval(50000),
                new ObjectiveFunction(objective),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{0.085, 10}),
                new NelderMeadSimplex(new double[]{0.05, 0.05}));
        return optimum.getPoint()[0];
    }


    private static class Objective implements MultivariateFunction {
        private final List<Integer> t;
        private final List<Integer> k;
        private final List<Integer> n;

        private Objective(List<Integer> t, List<Integer> k, List<Integer> n) {
            this.t = t;
            this.k = k;
            this.n = n;
        }

        @Override
        public double value(double[] variables) {
            final double a = variables[0];
            final double t0 = variables[1];
            List<Double> p = t.stream()
                    .map(_t -> Math.exp(((_t - t0) * a)) / (1 + Math.exp((_t - t0) * a)))
                    .collect(Collectors.toList());
            double sum = 0;
            for (int i = 0; i < t.size(); i++) {
                int _k = k.get(i);
                int _n = n.get(i);
                double _p = p.get(i);
                sum += _k * Math.log(_p) + (_n - _k) * Math.log(1 - _p);
            }
            return -sum;
        }
    }

}
