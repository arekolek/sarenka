
package com.github.arekolek.sarenka.edit;

import android.content.Context;
import android.text.format.DateFormat;
import com.github.arekolek.sarenka.Log;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class TimeCalendar {

    private Calendar calendar = Calendar.getInstance();

    public void setHour(int hour) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
    }

    public void setMinute(int minute) {
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

    public void fromTimeString(Context context, String time) {
        try {
            Date date = DateFormat.getTimeFormat(context).parse(time);
            calendar.setTime(date);
        } catch (ParseException e) {
            Log.e("Error parsing time: " + time + ", " + e.getMessage());
        }
    }

}
