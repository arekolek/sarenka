package com.github.arekolek.sarenka.ring;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.PowerManager;
import com.github.arekolek.sarenka.R;
import com.github.arekolek.sarenka.edit.Alarm;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    /**
     * If the alarm is older than STALE_WINDOW, ignore.  It
     * is probably the result of a time or timezone change
     */
    private final static int STALE_WINDOW = 30 * 60 * 1000;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final PendingResult result = goAsync();
        final PowerManager.WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        AsyncHandler.post(new Runnable() {
            @Override
            public void run() {
                handleIntent(context, intent);
                result.finish();
                wl.release();
            }
        });
    }

    private void handleIntent(Context context, Intent intent) {
        Alarm alarm = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }

        if (alarm == null) {
            Log.wtf("Failed to parse the alarm from the intent");
            // Make sure we set the next alert if needed.
            Alarms.Global.setNextAlert(context);
            return;
        }

        int alarmId = alarm.hashCode();

        // Disable this alarm if it does not repeat.
        if (!alarm.getDaysOfWeek().isRepeatSet()) {
            Alarms.enableAlarm(context, alarmId, false);
        } else {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
            Alarms.Global.setNextAlert(context);
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();
        Log.v("Received alarm set for id=" + alarmId + " " + Log.formatTime(alarm.time));

        // Always verbose to track down time change problems.
        if (now > alarm.time + STALE_WINDOW) {
            Log.v("Ignoring stale alarm");
            return;
        }

        // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
        // pick it up.
        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        /* Close dialogs and window shade */
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        // Play the alarm alert and vibrate the device.
        Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
        playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        context.startService(playAlarm);

        // Trigger a notification that, when clicked, will show the alarm alert
        // dialog. No need to check for fullscreen since this will always be
        // launched from a user action.
        Intent notify = new Intent(context, AlarmAlertFullScreen.class);
        notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        PendingIntent pendingNotify = PendingIntent.getActivity(context,
                alarmId, notify, 0);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(alarm.time);
        String alarmTime = Alarms.formatTime(context, cal);

        // Use the alarm's label or the default label main text of the notification.
        String label = alarm.getLabelOrDefault(context);

        Intent alarmAlert = new Intent(context, AlarmAlertFullScreen.class);
        alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        Notification n = new Notification.Builder(context)
                .setContentTitle(label)
                .setContentText(alarmTime)
                .setSmallIcon(R.drawable.stat_notify_alarm)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setFullScreenIntent(PendingIntent.getActivity(context, alarmId,
                        alarmAlert, PendingIntent.FLAG_UPDATE_CURRENT), true)
                .setContentIntent(pendingNotify)
                .setWhen(0)
                .build();

        // Send the notification using the alarm id to easily identify the
        // correct notification.
        NotificationManager nm = getNotificationManager(context);
        nm.notify(alarmId, n);
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

}
