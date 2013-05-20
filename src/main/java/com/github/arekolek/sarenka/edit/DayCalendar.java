package com.github.arekolek.sarenka.edit;

import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedSet;

public class DayCalendar {

    private Calendar calendar = Calendar.getInstance();

    public String getDayShortName(Integer day) {
        calendar.set(Calendar.DAY_OF_WEEK, day);
        return String.valueOf(DateFormat.format(String.valueOf(DateFormat.DAY), calendar.getTime()));
    }

    public List<String> convertDaysToNames(SortedSet<Integer> days) {
        ArrayList<String> dayNames = new ArrayList<String>();
        for (Integer day : days) {
            dayNames.add(getDayShortName(day));
        }
        return dayNames;
    }

}
