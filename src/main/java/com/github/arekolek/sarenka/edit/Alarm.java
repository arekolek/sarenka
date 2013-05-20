package com.github.arekolek.sarenka.edit;

import android.content.Context;
import android.text.TextUtils;
import com.orm.SugarRecord;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Alarm extends SugarRecord<Alarm> {

    public int hour;
    public int minute;
    public int days;
    public boolean enabled;
    public String label;
    public String sound;

    public Alarm(Context context) {
        super(context);
    }

    public String getFormattedTime(Context context) {
        TimeCalendar calendar = new TimeCalendar();
        calendar.setHour(hour);
        calendar.setMinute(minute);
        return calendar.getTimeString(context);
    }

    public String getFormattedLabel() {
        return TextUtils.isEmpty(label) ? null : days == 0 ? label : label + ": ";
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFormattedDays() {
        DayCalendar calendar = new DayCalendar();
        List<String> dayNames = calendar.convertDaysToNames(getDays());
        return TextUtils.join(", ", dayNames);
    }

    public void setDays(SortedSet<Integer> days) {
        this.days = 0;
        for (Integer day : days) {
            this.days |= 1 << day;
        }
    }

    public SortedSet<Integer> getDays() {
        TreeSet<Integer> days = new TreeSet<Integer>();
        for (int i = 1; i < 8; ++i) {
            if ((this.days & (1 << i)) != 0) {
                days.add(i);
            }
        }
        return days;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSound() {
        return sound;
    }
}
