package ch.ethz.covspectrum.entity.api;

import org.threeten.extra.YearWeek;


public class WeekAndZipCode {

    private final YearWeek week;

    private final String zipCode;


    public WeekAndZipCode(YearWeek week, String zipCode) {
        this.week = week;
        this.zipCode = zipCode;
    }


    public YearWeek getWeek() {
        return week;
    }


    public String getZipCode() {
        return zipCode;
    }
}
