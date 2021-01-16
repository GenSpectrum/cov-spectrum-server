package ch.ethz.vm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.apache.commons.math3.stat.interval.IntervalUtils;
import org.threeten.extra.YearWeek;

import java.util.Objects;


public class DistributionByWeekAndCountry {

    private final YearWeek week;

    private final String country;

    private final int count;

    private final int total;

    private final double p;

    private final double pLower;

    private final double pUpper;


    public DistributionByWeekAndCountry(YearWeek week, String country, int count, int total) {
        this.week = week;
        this.country = country;
        this.count = count;
        this.total = total;

        // Compute proportion with confidence internal
        this.p = count * 1.0 / total;
        ConfidenceInterval ci = IntervalUtils.getWilsonScoreInterval(total, count, 0.95);
        this.pLower = ci.getLowerBound();
        this.pUpper = ci.getUpperBound();
    }

    public YearWeek getWeek() {
        return week;
    }


    public String getCountry() {
        return country;
    }


    public int getCount() {
        return count;
    }

    public int getTotal() {
        return total;
    }


    public double getP() {
        return p;
    }


    @JsonProperty("pLower")
    public double getPLower() {
        return pLower;
    }


    @JsonProperty("pUpper")
    public double getPUpper() {
        return pUpper;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DistributionByWeekAndCountry that = (DistributionByWeekAndCountry) o;
        return count == that.count &&
                total == that.total &&
                Objects.equals(week, that.week) &&
                Objects.equals(country, that.country);
    }


    @Override
    public int hashCode() {
        return Objects.hash(week, country, count, total);
    }
}
