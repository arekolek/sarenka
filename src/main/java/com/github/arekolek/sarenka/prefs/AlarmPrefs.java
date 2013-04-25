
package com.github.arekolek.sarenka.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;

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

    public Integer getDays() {
        SortedSet<String> days = new TreeSet<String>(prefs.getStringSet(DAYS, null));
        for (String day : days) {
            Integer.parseInt(day);
        }
        return null;
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
            Set<String> days = prefs.getStringSet(key, null);
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

}
