
package com.github.arekolek.sarenka.edit;

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

public class AlarmSharedPreferences {

    public static AlarmSharedPreferences getPreferences(Context context) {
        return new AlarmSharedPreferences(context);
    }

    public static final String HOUR = "alarm_hour";
    public static final String DAYS = "alarm_days";
    public static final String LABEL = "alarm_label";
    public static final String SOUND = "alarm_sound";
    public static final String ENABLED = "alarm_enabled";
    public static final String BARCODE = "alarm_barcode";
    public static final String BARCODE_HINT = "alarm_barcode_hint";
    public static final String BARCODE_SCREEN = "alarm_barcode_screen";

    private SharedPreferences prefs;
    private Context context;

    public AlarmSharedPreferences(Context context) {
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
        calendar.fromTimeString(context, pref);
        return calendar;
    }

    private String getSummary(String key) {
        if (HOUR.equals(key)) {
            return prefs.getString(HOUR, "");
        }
        if (DAYS.equals(key)) {
            DayCalendar calendar = new DayCalendar();
            List<String> days = calendar.convertDaysToNames(getDays());
            return days == null || days.isEmpty() ? "Never" : TextUtils.join(", ", days);
        }
        if (LABEL.equals(key) || BARCODE.equals(key) || BARCODE_HINT.equals(key)) {
            String label = prefs.getString(key, null);
            return TextUtils.isEmpty(label) ? "None" : label;
        }
        if (SOUND.equals(key)) {
            String ringtoneUri = prefs.getString(key, null);
            if (TextUtils.isEmpty(ringtoneUri)) {
                // TODO silent is not allowed
                return "Silent";
            }
            Ringtone ringtone = RingtoneManager.getRingtone(context, Uri.parse(ringtoneUri));
            return ringtone != null ? ringtone.getTitle(context) : "Silent";
        }
        if (BARCODE_SCREEN.equals(key)) {
            String barcode = prefs.getString(BARCODE, null);
            String hint = prefs.getString(BARCODE_HINT, null);
            return TextUtils.isEmpty(barcode) ? "None" : TextUtils.isEmpty(hint) ? barcode : String.format("%s (%s)", hint, barcode);
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
        editor.putString(BARCODE, alarm.getBarcode());
        editor.putString(BARCODE_HINT, alarm.getBarcodeHint());
        editor.apply();
    }

    public void reset() {
        SharedPreferences.Editor editor = prefs.edit().clear();
        TimeCalendar calendar = new TimeCalendar();
        editor.putString(HOUR, calendar.getTimeString(context));
        editor.putString(SOUND, "content://settings/system/alarm_alert");
        editor.apply();
    }

    public void setBarcode(String barcode) {
        prefs.edit().putString(BARCODE, barcode).apply();
    }

    public String getBarcode() {
        return prefs.getString(BARCODE, null);
    }

    public void setBarcodeHint(String hint) {
        prefs.edit().putString(BARCODE_HINT, hint).apply();
    }

    public String getBarcodeHint() {
        return prefs.getString(BARCODE_HINT, null);
    }
}
