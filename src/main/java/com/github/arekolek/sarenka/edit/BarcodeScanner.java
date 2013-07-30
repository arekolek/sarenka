package com.github.arekolek.sarenka.edit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import com.github.arekolek.sarenka.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class BarcodeScanner extends Activity {
    private AlarmSharedPreferences prefs;
    private BarcodeFragment fragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = AlarmSharedPreferences.getPreferences(this);

        if (savedInstanceState == null) {
            fragment = new BarcodeFragment();
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment).commit();
        } else {
            fragment = (BarcodeFragment) getFragmentManager().findFragmentById(android.R.id.content);
        }

        if (savedInstanceState == null && TextUtils.isEmpty(prefs.getBarcode())) {
            scanCode();
        }
    }

    public void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && !TextUtils.isEmpty(scanResult.getContents())) {
            prefs.setBarcode(scanResult.getContents());
            prefs.setBarcodeLabel(null);
            fragment.onBarcodeSet();
        } else if (TextUtils.isEmpty(prefs.getBarcode())) {
            finish();
        }
    }

    public static class BarcodeFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private BarcodeScanner activity;
        private AlarmSharedPreferences prefs;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            this.activity = (BarcodeScanner) activity;
            this.prefs = AlarmSharedPreferences.getPreferences(activity);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_add_barcode);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                    this);
            updateUi();
        }

        private void updateUi() {
            PreferenceScreen screen = getPreferenceScreen();
            for (int i = 0; i < screen.getPreferenceCount(); ++i) {
                prefs.setSummary(screen.getPreference(i));
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (AlarmSharedPreferences.BARCODE.equals(preference.getKey())) {
                activity.scanCode();
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public void onBarcodeSet() {
            EditTextPreference2 label = (EditTextPreference2) findPreference(AlarmSharedPreferences.BARCODE_LABEL);
            label.setText(null);
            label.show();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (findPreference(key) != null) {
                prefs.setSummary(findPreference(key));
            }
        }

    }

}