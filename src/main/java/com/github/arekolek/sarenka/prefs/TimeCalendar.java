
package com.github.arekolek.sarenka.prefs;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;

public class TimeCalendar {

    private Calendar calendar = Calendar.getInstance();

    public void setHour(Integer hour) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
    }

    public void setMinute(Integer minute) {
        calendar.set(Calendar.MINUTE, minute);
    }

    public Integer getHour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public Integer getMinute() {
        return calendar.get(Calendar.MINUTE);
    }

    public String getTimeString(Context context) {
        return DateFormat.getTimeFormat(context).format(calendar.getTime());
    }

    public void fromTimeString(String time) {
        String[] parts = time.split(":");
        setHour(Integer.parseInt(parts[0]));
        setMinute(Integer.parseInt(parts[1]));
    }

}
