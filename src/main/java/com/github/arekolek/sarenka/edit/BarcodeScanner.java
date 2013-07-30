package com.github.arekolek.sarenka.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import com.github.arekolek.sarenka.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class BarcodeScanner extends Activity {
    private AlarmSharedPreferences prefs;
    private BarcodeFragment fragment = new BarcodeFragment();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = AlarmSharedPreferences.getPreferences(this);

        if (savedInstanceState == null && TextUtils.isEmpty(prefs.getBarcode())) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment).commit();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            prefs.setBarcode(scanResult.getContents());
            prefs.setBarcodeLabel(null);
            fragment.showDialog();
        }
    }

    public static class BarcodeFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_add_barcode);
        }

        public void showDialog() {
            ((EditTextPreference2) findPreference(AlarmSharedPreferences.BARCODE_LABEL)).show();
        }
    }

}