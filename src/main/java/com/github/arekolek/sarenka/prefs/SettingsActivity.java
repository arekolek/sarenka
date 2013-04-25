
package com.github.arekolek.sarenka.prefs;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import com.github.arekolek.sarenka.R;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener {
        private AlarmSwitcher alarmSwitcher;
        private AlarmPrefs prefs;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_add_alarm);

            alarmSwitcher = new AlarmSwitcher(getActivity());

            prefs = AlarmPrefs.getPreferences(getActivity());

            setHasOptionsMenu(true);

            showDoneActionButton();

            updateSummaries();
        }

        private void showDoneActionButton() {
            // Inflate a "Done" custom action bar view to serve as the "Up" affordance.
            LayoutInflater inflater = (LayoutInflater) getActivity().getActionBar()
                    .getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done,
                    null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // "Done"
                            save();
                        }
                    });

            // Show the custom action bar view and hide the normal Home icon and title.
            final ActionBar actionBar = getActivity().getActionBar();
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                            | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
        }

        protected void save() {
            Alarm alarm = new Alarm(getActivity());

            alarm.hour = prefs.getHour();
            alarm.minute = prefs.getMinute();
            alarm.days = prefs.getDays();

            getActivity().finish();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.pref_alarm, menu);
            Switch masterSwitch = (Switch) menu.findItem(R.id.menu_alarm_enabled).getActionView();
            alarmSwitcher.setSwitch(masterSwitch);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_discard:
                    getActivity().finish();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onResume() {
            super.onResume();
            alarmSwitcher.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                    this);
            updateRingtoneSummary();
        }

        private void updateSummaries() {
            PreferenceScreen screen = getPreferenceScreen();
            for (int i = 0; i < screen.getPreferenceCount(); ++i) {
                prefs.setSummary(screen.getPreference(i));
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            alarmSwitcher.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (findPreference(key) != null) {
                prefs.setSummary(findPreference(key));
            }
        }

        private void updateRingtoneSummary() {
            prefs.setSummary(findPreference(AlarmPrefs.SOUND));
        }

    }
}
