package com.github.arekolek.sarenka.ring;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.provider.Settings;
import android.text.format.DateFormat;
import com.github.arekolek.sarenka.edit.Alarm;

import java.util.Calendar;
import java.util.List;

public class Alarms {

    // TODO update description
    // This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    public static final String ALARM_ALERT_ACTION = "com.github.arekolek.sarenka.ALARM_ALERT";

    // This string is used when passing an Alarm object through an intent.
    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    // A public action sent by AlarmKlaxon when the alarm has stopped sounding
    // for any reason (e.g. because it has been dismissed from AlarmAlertFullScreen,
    // or killed due to an incoming phone call, etc).
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";

    // AlarmAlertFullScreen listens for this broadcast intent, so that other applications
    // can dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
    public static final String ALARM_DISMISS_ACTION = "com.github.arekolek.sarenka.ALARM_DISMISS";


    // This is a private action used by the AlarmKlaxon to update the UI to
    // show the alarm has been killed.
    public static final String ALARM_KILLED = "alarm_killed";

    // Extra in the ALARM_KILLED intent to indicate to the user how long the
    // alarm played before being killed.
    public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";

    // Extra in the ALARM_KILLED intent to indicate when alarm was replaced
    public static final String ALARM_REPLACED = "alarm_replaced";

    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    private final static String M12 = "h:mm aa";
    private final static String M24 = "kk:mm";

    private final static String DM12 = "E h:mm aa";
    private final static String DM24 = "E kk:mm";

    /**
     * @return true if clock is set to 24-hour mode
     */
    public static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }


    /**
     * A convenience method to set an alarm in the Alarms
     * content provider.
     *
     * @return Time when the alarm will fire. Or < 1 if update failed.
     */
    public static long saveAlarm(Context context, Alarm alarm) {
        alarm.save();

        long timeInMillis = calculateAlert(alarm);

        Global.setNextAlert(context);

        return timeInMillis;
    }

    public static void deleteAlarm(Context context, Alarm alarm) {
        alarm.delete();

        Global.setNextAlert(context);
    }

    /**
     * A convenience method to enable or disable an alarm.
     *
     * @param id      corresponds to the _id column
     * @param enabled corresponds to the ENABLED column
     */
    public static void enableAlarm(final Context context, final long id, boolean enabled) {
        enableAlarmInternal(id, enabled);
        Global.setNextAlert(context);
    }

    // TODO call at boot

    /**
     * Disables non-repeating alarms that have passed.  Called at
     * boot.
     */
    public static void disableExpiredAlarms() {
        List<Alarm> alarms = loadEnabledAlarms();
        long now = System.currentTimeMillis();

        for (Alarm alarm : alarms) {
            // A time of 0 means this alarm repeats. If the time is
            // non-zero, check if the time is before now.
            if (alarm.time != 0 && alarm.time < now) {
                Log.v("Disabling expired alarm set for " + Log.formatTime(alarm.time));
                enableAlarmInternal(alarm, false);
            }
        }
    }

    /**
     * Return an Alarm object representing the alarm id in the database.
     * Returns null if no alarm exists.
     */
    public static Alarm loadAlarm(long alarmId) {
        return Alarm.findById(Alarm.class, alarmId);
    }

    public static List<Alarm> loadAllAlarms() {
        return Alarm.listAll(Alarm.class);
    }

    private static List<Alarm> loadEnabledAlarms() {
        return Alarm.find(Alarm.class, "enabled = ?", new String[]{"true"});
    }

    private static void enableAlarmInternal(final long id, boolean enabled) {
        enableAlarmInternal(loadAlarm(id), enabled);
    }

    private static void enableAlarmInternal(final Alarm alarm, boolean enabled) {
        if (alarm == null) {
            return;
        }

        ContentValues values = new ContentValues(2);

        alarm.enabled = enabled;

        // If we are enabling the alarm, calculate alarm time since the time
        // value in Alarm may be old.
        if (enabled) {
            long time = 0;
            if (!alarm.getDaysOfWeek().isRepeatSet()) {
                time = calculateAlert(alarm);
            }
            alarm.time = time;
        }

        alarm.save();
    }

    private static Alarm calculateNextAlert() {
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();

        List<Alarm> alarms = loadEnabledAlarms();

        Alarm alarm = null;

        for (Alarm a : alarms) {
            a.time = calculateAlert(a);

            if (a.time < now) {
                // TODO not sure what this should do in my case
                // Expired alarm, disable it and move along.
                enableAlarmInternal(a, false);
                continue;
            }
            if (a.time < minTime) {
                minTime = a.time;
                alarm = a;
            }
        }

        return alarm;
    }

    private static long calculateAlert(Alarm alarm) {
        return calculateAlert(alarm.hour, alarm.minute, alarm.getDaysOfWeek())
                .getTimeInMillis();
    }

    /**
     * Given an alarm in hours and minutes, return a time suitable for
     * setting in AlarmManager.
     */
    private static Calendar calculateAlert(int hour, int minute,
                                           Alarm.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour ||
                hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0) c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }

    private static String formatTime(final Context context, int hour, int minute,
                                     Alarm.DaysOfWeek daysOfWeek) {
        Calendar c = calculateAlert(hour, minute, daysOfWeek);
        return formatTime(context, c);
    }

    /* used by AlarmAlert */
    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String) DateFormat.format(format, c);
    }

    /**
     * Shows day and time -- used for lock screen
     */
    private static String formatDayAndTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? DM24 : DM12;
        return (c == null) ? "" : (String) DateFormat.format(format, c);
    }

    public static final class Global {

        // TODO call at boot / timezone change

        /**
         * Called at system startup, on time/timezone change, and whenever
         * the user changes alarm settings. Loads all alarms, activates next alert.
         */
        public static void setNextAlert(final Context context) {
            final Alarm alarm = calculateNextAlert();
            if (alarm != null) {
                enableAlert(context, alarm, alarm.time);
            } else {
                disableAlert(context);
            }
//        Intent i = new Intent(NEXT_ALARM_TIME_SET);
//        context.sendBroadcast(i);
        }

        /**
         * Sets alert in AlarmManger and StatusBar.  This is what will
         * actually launch the alert when the alarm triggers.
         *
         * @param alarm          Alarm.
         * @param atTimeInMillis milliseconds since epoch
         */
        private static void enableAlert(Context context, final Alarm alarm,
                                        final long atTimeInMillis) {
            AlarmManager am = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);

            // Intentionally verbose: always log the alarm time to provide useful
            // information in bug reports.
            Log.v("Alarm set for id=" + alarm.getId() + " " + Log.formatTime(atTimeInMillis));

            Intent intent = new Intent(ALARM_ALERT_ACTION);

            // XXX: This is a slight hack to avoid an exception in the remote
            // AlarmManagerService process. The AlarmManager adds extra data to
            // this Intent which causes it to inflate. Since the remote process
            // does not know about the Alarm class, it throws a
            // ClassNotFoundException.
            //
            // To avoid this, we marshall the data ourselves and then parcel a plain
            // byte[] array. The AlarmReceiver class knows to build the Alarm
            // object from the byte[] array.
            Parcel out = Parcel.obtain();
            alarm.writeToParcel(out, 0);
            out.setDataPosition(0);
            intent.putExtra(ALARM_RAW_DATA, out.marshall());

            PendingIntent sender = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

            setStatusBarIcon(context, true);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(atTimeInMillis);
            String timeString = formatDayAndTime(context, c);
            saveNextAlert(context, timeString);
        }

        /**
         * Disables alert in AlarmManager and StatusBar.
         *
         * @param context The context
         */
        private static void disableAlert(Context context) {
            AlarmManager am = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(
                    context, 0, new Intent(ALARM_ALERT_ACTION),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            am.cancel(sender);
            setStatusBarIcon(context, false);
            // Intentionally verbose: always log the lack of a next alarm to provide useful
            // information in bug reports.
            Log.v("No next alarm");
            saveNextAlert(context, "");
        }

        /**
         * Save time of the next alarm, as a formatted string, into the system
         * settings so those who care can make use of it.
         */
        private static void saveNextAlert(final Context context, String timeString) {
            Settings.System.putString(context.getContentResolver(),
                    Settings.System.NEXT_ALARM_FORMATTED,
                    timeString);
        }


        /**
         * Tells the StatusBar whether the alarm is enabled or disabled
         */
        private static void setStatusBarIcon(Context context, boolean enabled) {
            Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
            alarmChanged.putExtra("alarmSet", enabled);
            context.sendBroadcast(alarmChanged);
        }

    }
}
