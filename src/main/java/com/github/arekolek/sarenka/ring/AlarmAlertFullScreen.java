package com.github.arekolek.sarenka.ring;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.github.arekolek.sarenka.R;
import com.github.arekolek.sarenka.edit.Alarm;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AlarmAlertFullScreen extends Activity {
    private static final boolean LOG = true;
    protected static final String SCREEN_OFF = "screen_off";
    private Alarm alarm;
    private boolean barcodeChallenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_alert);

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

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        alarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        barcodeChallenge = !TextUtils.isEmpty(alarm.getBarcode());

        if (barcodeChallenge) {
            ((TextView) findViewById(R.id.hint)).setText(getString(R.string.barcode_hint, alarm.getBarcodeHint(), alarm.getBarcode()));
        }

        ((TextView) findViewById(R.id.alertTitle)).setText(alarm.getLabelOrDefault(this));
    }

    public void onDismissClicked(View view) {
        if (barcodeChallenge) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        } else {
            dismiss(false, false);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && !TextUtils.isEmpty(scanResult.getContents())) {
            if (TextUtils.equals(alarm.getBarcode(), scanResult.getContents())) {
                dismiss(false, false);
            } else {
                Toast.makeText(this, getString(R.string.barcode_wrong, alarm.getBarcodeHint(), alarm.getBarcode()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    // Dismiss the alarm.
    private void dismiss(boolean killed, boolean replaced) {
        if (LOG) {
            Log.v("AlarmAlertFullScreen - dismiss");
        }

        Log.i("Alarm id=" + alarm.getId() + (killed ? (replaced ? " replaced" : " killed") : " dismissed by user"));
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(alarm.hashCode());
            stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        }
        if (!replaced) {
            finish();
        }
    }

}
