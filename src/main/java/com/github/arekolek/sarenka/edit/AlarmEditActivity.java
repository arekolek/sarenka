
package com.github.arekolek.sarenka.edit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.*;
import android.widget.Switch;
import com.github.arekolek.sarenka.R;
import com.github.arekolek.sarenka.ring.Alarms;

public class AlarmEditActivity extends Activity {
    public static final String EXTRA_ALARM_ID = "alarm_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            AlarmSharedPreferences.getPreferences(this).reset();
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static void startActivity(Context context, long alarmId) {
        Intent intent = new Intent(context, AlarmEditActivity.class);
        intent.putExtra(EXTRA_ALARM_ID, alarmId);
        context.startActivity(intent);
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, AlarmEditActivity.class));
    }

    public static class SettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener {
        private AlarmSwitcher alarmSwitcher;
        private AlarmSharedPreferences prefs;
        private Alarm alarm;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            prefs = AlarmSharedPreferences.getPreferences(getActivity());
            alarmSwitcher = new AlarmSwitcher(getActivity());

            // Load the preferences from an XML resource
            long alarmId = getActivity().getIntent().getLongExtra(EXTRA_ALARM_ID, -1);
            if (alarmId > -1) {
                load(alarmId);
            }

            addPreferencesFromResource(R.xml.pref_add_alarm);

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

        private void load(long alarmId) {
            alarm = Alarms.loadAlarm(alarmId);
            prefs.fromAlarm(alarm);
        }

        protected void save() {
            if (alarm == null) {
                alarm = new Alarm(getActivity());
            }

            alarm.hour = prefs.getHour();
            alarm.minute = prefs.getMinute();
            alarm.setDays(prefs.getDays());
            alarm.label = prefs.getLabel();
            alarm.sound = prefs.getSound();
            alarm.enabled = prefs.isEnabled();

            Alarms.saveAlarm(getActivity(), alarm);

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
            prefs.setSummary(findPreference(AlarmSharedPreferences.SOUND));
        }

    }
}
