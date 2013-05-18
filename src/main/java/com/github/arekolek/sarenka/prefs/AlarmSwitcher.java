
package com.github.arekolek.sarenka.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class AlarmSwitcher implements OnCheckedChangeListener {

    private Switch mSwitch;
    private SharedPreferences prefs;

    public AlarmSwitcher(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        prefs.edit().putBoolean(AlarmPrefs.ENABLED, isChecked).commit();
    }

    public void onResume() {
        if (mSwitch != null) {
            mSwitch.setOnCheckedChangeListener(this);
            mSwitch.setChecked(isEnabled());
        }
    }

    public void onPause() {
        if (mSwitch != null) {
            mSwitch.setOnCheckedChangeListener(null);
        }
    }

    public void setSwitch(Switch swtch) {
        if (mSwitch == swtch) {
            return;
        }

        if (mSwitch != null) {
            mSwitch.setOnCheckedChangeListener(null);
        }
        mSwitch = swtch;
        onResume();
    }

    public boolean isEnabled() {
        return prefs.getBoolean(AlarmPrefs.ENABLED, true);
    }

}
