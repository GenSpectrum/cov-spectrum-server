package ch.ethz.covspectrum.entity.model.chen2021fitness;

import java.time.LocalDate;
import java.util.List;


public class ApiResponse {

    public static class ValueWithCI {

        private float value;

        private float ciLower;

        private float ciUpper;

        public ValueWithCI() {
        }

        public ValueWithCI(float value, float ciLower, float ciUpper) {
            this.value = value;
            this.ciLower = ciLower;
            this.ciUpper = ciUpper;
        }

        public float getValue() {
            return value;
        }

        public ValueWithCI setValue(float value) {
            this.value = value;
            return this;
        }

        public float getCiLower() {
            return ciLower;
        }

        public ValueWithCI setCiLower(float ciLower) {
            this.ciLower = ciLower;
            return this;
        }

        public float getCiUpper() {
            return ciUpper;
        }

        public ValueWithCI setCiUpper(float ciUpper) {
            this.ciUpper = ciUpper;
            return this;
        }
    }

    public static class Daily {
        private List<LocalDate> t;

        private List<Float> proportion;

        private List<Float> ciLower;

        private List<Float> ciUpper;

        public Daily() {
        }

        public Daily(List<LocalDate> t, List<Float> proportion, List<Float> ciLower, List<Float> ciUpper) {
            this.t = t;
            this.proportion = proportion;
            this.ciLower = ciLower;
            this.ciUpper = ciUpper;
        }

        public List<LocalDate> getT() {
            return t;
        }

        public Daily setT(List<LocalDate> t) {
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

        public List<Float> getCiLower() {
            return ciLower;
        }

        public Daily setCiLower(List<Float> ciLower) {
            this.ciLower = ciLower;
            return this;
        }

        public List<Float> getCiUpper() {
            return ciUpper;
        }

        public Daily setCiUpper(List<Float> ciUpper) {
            this.ciUpper = ciUpper;
            return this;
        }
    }

    public static class Params {

        private ValueWithCI a;

        private ValueWithCI t0;

        private ValueWithCI fc;

        private ValueWithCI fd;

        public Params() {
        }

        public Params(ValueWithCI a, ValueWithCI t0, ValueWithCI fc, ValueWithCI fd) {
            this.a = a;
            this.t0 = t0;
            this.fc = fc;
            this.fd = fd;
        }

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
        private List<LocalDate> t;
        private List<Integer> variantCases;
        private List<Integer> wildtypeCases;

        public PlotAbsoluteNumbers() {
        }

        public PlotAbsoluteNumbers(List<LocalDate> t, List<Integer> variantCases, List<Integer> wildtypeCases) {
            this.t = t;
            this.variantCases = variantCases;
            this.wildtypeCases = wildtypeCases;
        }

        public List<LocalDate> getT() {
            return t;
        }

        public PlotAbsoluteNumbers setT(List<LocalDate> t) {
            this.t = t;
            return this;
        }

        public List<Integer> getVariantCases() {
            return variantCases;
        }

        public PlotAbsoluteNumbers setVariantCases(List<Integer> variantCases) {
            this.variantCases = variantCases;
            return this;
        }

        public List<Integer> getWildtypeCases() {
            return wildtypeCases;
        }

        public PlotAbsoluteNumbers setWildtypeCases(List<Integer> wildtypeCases) {
            this.wildtypeCases = wildtypeCases;
            return this;
        }
    }

    public static class PlotProportion {
        private List<LocalDate> t;

        private List<Float> proportion;

        private List<Float> ciLower;

        private List<Float> ciUpper;

        public PlotProportion() {
        }

        public PlotProportion(List<LocalDate> t, List<Float> proportion, List<Float> ciLower, List<Float> ciUpper) {
            this.t = t;
            this.proportion = proportion;
            this.ciLower = ciLower;
            this.ciUpper = ciUpper;
        }

        public List<LocalDate> getT() {
            return t;
        }

        public PlotProportion setT(List<LocalDate> t) {
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

        public List<Float> getCiLower() {
            return ciLower;
        }

        public PlotProportion setCiLower(List<Float> ciLower) {
            this.ciLower = ciLower;
            return this;
        }

        public List<Float> getCiUpper() {
            return ciUpper;
        }

        public PlotProportion setCiUpper(List<Float> ciUpper) {
            this.ciUpper = ciUpper;
            return this;
        }
    }


    private Daily daily;

    private Params params;

    private PlotAbsoluteNumbers plotAbsoluteNumbers;

    private PlotProportion plotProportion;


    public ApiResponse() {
    }


    public ApiResponse(
            Daily daily,
            Params params,
            PlotAbsoluteNumbers plotAbsoluteNumbers,
            PlotProportion plotProportion
    ) {
        this.daily = daily;
        this.params = params;
        this.plotAbsoluteNumbers = plotAbsoluteNumbers;
        this.plotProportion = plotProportion;
    }

    public Daily getDaily() {
        return daily;
    }


    public ApiResponse setDaily(Daily daily) {
        this.daily = daily;
        return this;
    }


    public Params getParams() {
        return params;
    }

    public ApiResponse setParams(Params params) {
        this.params = params;
        return this;
    }


    public PlotAbsoluteNumbers getPlotAbsoluteNumbers() {
        return plotAbsoluteNumbers;
    }


    public ApiResponse setPlotAbsoluteNumbers(PlotAbsoluteNumbers plotAbsoluteNumbers) {
        this.plotAbsoluteNumbers = plotAbsoluteNumbers;
        return this;
    }


    public PlotProportion getPlotProportion() {
        return plotProportion;
    }


    public ApiResponse setPlotProportion(PlotProportion plotProportion) {
        this.plotProportion = plotProportion;
        return this;
    }
}
