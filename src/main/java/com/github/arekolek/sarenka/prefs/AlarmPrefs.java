
package com.github.arekolek.sarenka.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class AlarmPrefs {

    public static AlarmPrefs getPreferences(Context context) {
        return new AlarmPrefs(context);
    }

    public static final String HOUR = "alarm_hour";
    public static final String DAYS = "alarm_days";
    public static final String LABEL = "alarm_label";
    public static final String SOUND = "alarm_sound";
    public static final String ENABLED = "alarm_enabled";

    private SharedPreferences prefs;
    private Context context;

    public AlarmPrefs(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Integer getHour() {
        return getTime().getHour();
    }

    public Integer getMinute() {
        return getTime().getMinute();
    }

    /**
     * Returns a list of days the alarm should be active on. See Calendar.DAY_OF_WEEK
     */
    public SortedSet<Integer> getDays() {
        SortedSet<String> days = new TreeSet<String>(prefs.getStringSet(DAYS, null));
        TreeSet<Integer> result = new TreeSet<Integer>();
        for (String day : days) {
            result.add(Integer.parseInt(day));
        }
        return result;
    }

    public String getLabel() {
        return prefs.getString(LABEL, null);
    }

    public String getSound() {
        return prefs.getString(SOUND, null);
    }

    public Boolean isEnabled() {
        return prefs.getBoolean(ENABLED, true);
    }

    public void setSummary(Preference preference) {
        preference.setSummary(getSummary(preference.getKey()));
    }

    private TimeCalendar getTime() {
        String pref = prefs.getString(HOUR, null);
        if (TextUtils.isEmpty(pref)) {
            return null;
        }
        TimeCalendar calendar = new TimeCalendar();
        calendar.fromTimeString(pref);
        return calendar;
    }

    private String getSummary(String key) {
        if (key.equals(HOUR)) {
            return prefs.getString(HOUR, "");
        }
        if (key.equals(DAYS)) {
            DayCalendar calendar = new DayCalendar();
            List<String> days = calendar.convertDaysToNames(getDays());
            return days == null || days.isEmpty() ? "Never" : TextUtils.join(", ", days);
        }
        if (key.equals(LABEL)) {
            String label = prefs.getString(key, null);
            return TextUtils.isEmpty(label) ? "None" : label;
        }
        if (key.equals(SOUND)) {
            String ringtoneUri = prefs.getString(key, null);
            if (TextUtils.isEmpty(ringtoneUri)) {
                return "Silent";
            }
            Ringtone ringtone = RingtoneManager.getRingtone(context, Uri.parse(ringtoneUri));
            return ringtone != null ? ringtone.getTitle(context) : "Silent";
        }
        return "";
    }

    public void fromAlarm(Alarm alarm) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(HOUR, alarm.getFormattedTime(context));
        Set<String> days = new TreeSet<String>();
        Set<Integer> dayNames = alarm.getDays();
        for (Integer day : dayNames) {
            days.add(String.valueOf(day));
        }
        editor.putStringSet(DAYS, days);
        editor.putString(LABEL, alarm.getLabel());
        editor.putString(SOUND, alarm.getSound());
        editor.putBoolean(ENABLED, alarm.isEnabled());
        editor.apply();
    }
}
