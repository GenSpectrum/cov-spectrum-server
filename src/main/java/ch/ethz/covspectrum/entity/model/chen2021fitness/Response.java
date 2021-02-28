package ch.ethz.covspectrum.entity.model.chen2021fitness;

import java.util.List;


public class Response {

    public static class ValueWithCI {

        private float value;

        private float ci_lower;

        private float ci_upper;

        public float getValue() {
            return value;
        }

        public ValueWithCI setValue(float value) {
            this.value = value;
            return this;
        }

        public float getCi_lower() {
            return ci_lower;
        }

        public ValueWithCI setCi_lower(float ci_lower) {
            this.ci_lower = ci_lower;
            return this;
        }

        public float getCi_upper() {
            return ci_upper;
        }

        public ValueWithCI setCi_upper(float ci_upper) {
            this.ci_upper = ci_upper;
            return this;
        }
    }

    public static class Daily {
        private List<Integer> t;

        private List<Float> proportion;

        private List<Float> ci_lower;

        private List<Float> ci_upper;

        public Daily() {
        }

        public List<Integer> getT() {
            return t;
        }

        public Daily setT(List<Integer> t) {
            this.t = t;
            return this;
        }

        public List<Float> getProportion() {
            return proportion;
        }

        public Daily setProportion(List<Float> proportion) {
            this.proportion = proportion;
            return this;
        }

        public List<Float> getCi_lower() {
            return ci_lower;
        }

        public Daily setCi_lower(List<Float> ci_lower) {
            this.ci_lower = ci_lower;
            return this;
        }

        public List<Float> getCi_upper() {
            return ci_upper;
        }

        public Daily setCi_upper(List<Float> ci_upper) {
            this.ci_upper = ci_upper;
            return this;
        }
    }

    public static class Params {

        private ValueWithCI a;

        private ValueWithCI t0;

        private ValueWithCI fc;

        private ValueWithCI fd;

        public ValueWithCI getA() {
            return a;
        }

        public Params setA(ValueWithCI a) {
            this.a = a;
            return this;
        }

        public ValueWithCI getT0() {
            return t0;
        }

        public Params setT0(ValueWithCI t0) {
            this.t0 = t0;
            return this;
        }

        public ValueWithCI getFc() {
            return fc;
        }

        public Params setFc(ValueWithCI fc) {
            this.fc = fc;
            return this;
        }

        public ValueWithCI getFd() {
            return fd;
        }

        public Params setFd(ValueWithCI fd) {
            this.fd = fd;
            return this;
        }
    }

    public static class PlotAbsoluteNumbers {
        private List<Integer> t;
        private List<Integer> variant_cases;
        private List<Integer> wildtype_cases;

        public List<Integer> getT() {
            return t;
        }

        public PlotAbsoluteNumbers setT(List<Integer> t) {
            this.t = t;
            return this;
        }

        public List<Integer> getVariant_cases() {
            return variant_cases;
        }

        public PlotAbsoluteNumbers setVariant_cases(List<Integer> variant_cases) {
            this.variant_cases = variant_cases;
            return this;
        }

        public List<Integer> getWildtype_cases() {
            return wildtype_cases;
        }

        public PlotAbsoluteNumbers setWildtype_cases(List<Integer> wildtype_cases) {
            this.wildtype_cases = wildtype_cases;
            return this;
        }
    }

    public static class PlotProportion {
        private List<Integer> t;

        private List<Float> proportion;

        private List<Float> ci_lower;

        private List<Float> ci_upper;

        public List<Integer> getT() {
            return t;
        }

        public PlotProportion setT(List<Integer> t) {
            this.t = t;
            return this;
        }

        public List<Float> getProportion() {
            return proportion;
        }

        public PlotProportion setProportion(List<Float> proportion) {
            this.proportion = proportion;
            return this;
        }

        public List<Float> getCi_lower() {
            return ci_lower;
        }

        public PlotProportion setCi_lower(List<Float> ci_lower) {
            this.ci_lower = ci_lower;
            return this;
        }

        public List<Float> getCi_upper() {
            return ci_upper;
        }

        public PlotProportion setCi_upper(List<Float> ci_upper) {
            this.ci_upper = ci_upper;
            return this;
        }
    }


    private Daily daily;

    private Params params;

    private PlotAbsoluteNumbers plot_absolute_numbers;

    private PlotProportion plot_proportion;


    public Daily getDaily() {
        return daily;
    }


    public Response setDaily(Daily daily) {
        this.daily = daily;
        return this;
    }


    public Params getParams() {
        return params;
    }

    public Response setParams(Params params) {
        this.params = params;
        return this;
    }


    public PlotAbsoluteNumbers getPlot_absolute_numbers() {
        return plot_absolute_numbers;
    }


    public Response setPlot_absolute_numbers(PlotAbsoluteNumbers plot_absolute_numbers) {
        this.plot_absolute_numbers = plot_absolute_numbers;
        return this;
    }


    public PlotProportion getPlot_proportion() {
        return plot_proportion;
    }


    public Response setPlot_proportion(PlotProportion plot_proportion) {
        this.plot_proportion = plot_proportion;
        return this;
    }
}
