package ch.ethz.covspectrum.entity.model.chen2021fitness;

import java.util.List;

public class Request {

    public static class InnerData {
        private final List<Integer> t;
        private final List<Integer> n;
        private final List<Integer> k;

        public InnerData(List<Integer> t, List<Integer> n, List<Integer> k) {
            this.t = t;
            this.n = n;
            this.k = k;
        }

        public List<Integer> getT() {
            return t;
        }

        public List<Integer> getN() {
            return n;
        }

        public List<Integer> getK() {
            return k;
        }
    }


    private final InnerData data;

    private final float alpha;

    private final float generationTime;

    private final float reproductionNumberWildtype;

    private final int plotStartT;

    private final int plotEndT;

    private final int initialWildtype;

    private final int initialVariant;


    public Request(
            InnerData data,
            float alpha,
            float generationTime,
            float reproductionNumberWildtype,
            int plotStartT,
            int plotEndT,
            int initialWildtype,
            int initialVariant
    ) {
        this.data = data;
        this.alpha = alpha;
        this.generationTime = generationTime;
        this.reproductionNumberWildtype = reproductionNumberWildtype;
        this.plotStartT = plotStartT;
        this.plotEndT = plotEndT;
        this.initialWildtype = initialWildtype;
        this.initialVariant = initialVariant;
    }


    public InnerData getData() {
        return data;
    }

    public float getAlpha() {
        return alpha;
    }


    public float getGenerationTime() {
        return generationTime;
    }


    public float getReproductionNumberWildtype() {
        return reproductionNumberWildtype;
    }


    public int getPlotStartT() {
        return plotStartT;
    }


    public int getPlotEndT() {
        return plotEndT;
    }


    public int getInitialWildtype() {
        return initialWildtype;
    }


    public int getInitialVariant() {
        return initialVariant;
    }
}
