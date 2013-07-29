package com.github.arekolek.sarenka.ring;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.github.arekolek.sarenka.R;
import com.github.arekolek.sarenka.edit.Alarm;

public class AlarmAlertFullScreen extends Activity {
    private static final boolean LOG = true;
    protected static final String SCREEN_OFF = "screen_off";
    private Alarm mAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_alert);

        mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass as a result of the screen turning off.
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
    }

    public void onDismissClicked(View view) {
        dismiss(false, false);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    // Dismiss the alarm.
    private void dismiss(boolean killed, boolean replaced) {
        if (LOG) {
            Log.v("AlarmAlertFullScreen - dismiss");
        }

        Log.i("Alarm id=" + mAlarm.getId() + (killed ? (replaced ? " replaced" : " killed") : " dismissed by user"));
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(mAlarm.hashCode());
            stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        }
        if (!replaced) {
            finish();
        }
    }

}
