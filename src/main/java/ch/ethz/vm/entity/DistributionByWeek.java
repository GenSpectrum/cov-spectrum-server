package ch.ethz.vm.entity;

import org.threeten.extra.YearWeek;

import java.util.Objects;


public class DistributionByWeek {

    private final YearWeek week;

    private final int count;

    private final double proportion;


    public DistributionByWeek(YearWeek week, int count, double proportion) {
        this.week = week;
        this.count = count;
        this.proportion = proportion;
    }


    public YearWeek getWeek() {
        return week;
    }


    public int getCount() {
        return count;
    }


    public double getProportion() {
        return proportion;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DistributionByWeek that = (DistributionByWeek) o;
        return count == that.count && Double.compare(that.proportion, proportion) == 0 && Objects.equals(week, that.week);
    }


    @Override
    public int hashCode() {
        return Objects.hash(week, count, proportion);
    }
}
