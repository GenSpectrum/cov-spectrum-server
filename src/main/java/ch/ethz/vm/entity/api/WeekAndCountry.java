package ch.ethz.vm.entity.api;

import org.threeten.extra.YearWeek;


public class WeekAndCountry {

    private final YearWeek week;

    private final String country;


    public WeekAndCountry(YearWeek week, String country) {
        this.week = week;
        this.country = country;
    }


    public YearWeek getWeek() {
        return week;
    }


    public String getCountry() {
        return country;
    }
}
