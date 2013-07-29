package com.github.arekolek.sarenka.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.github.arekolek.sarenka.R;
import com.github.arekolek.sarenka.edit.Alarm;
import com.github.arekolek.sarenka.ring.Alarms;

import java.util.List;

public class AlarmAdapter extends ArrayAdapter<Alarm> {
    private final LayoutInflater inflater;
    private final Context context;

    public AlarmAdapter(Context context, List<Alarm> list) {
        super(context, R.layout.item_alarm, list);
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_alarm, parent, false);
        }
        AlarmViewCache cache = AlarmViewCache.from(view);
        Alarm alarm = getItem(position);
        cache.time.setText(alarm.getFormattedTime(context));
        cache.enabled.setChecked(alarm.isEnabled());
        cache.enabled.setOnCheckedChangeListener(new AlarmEnabler(context, alarm));
        cache.label.setText(alarm.getFormattedLabel());
        cache.days.setText(alarm.getFormattedDays());
        return view;
    }

    public void swapList(List<Alarm> alarms) {
        clear();
        addAll(alarms);
    }

    private static class AlarmViewCache {
        private TextView label;
        private TextView time;
        private TextView days;
        private Switch enabled;

        public static AlarmViewCache from(View view) {
            Object tag = view.getTag();
            if (tag != null && tag instanceof AlarmViewCache) {
                return (AlarmViewCache) tag;
            }
            AlarmViewCache cache = new AlarmViewCache();
            cache.label = (TextView) view.findViewById(R.id.label);
            cache.time = (TextView) view.findViewById(R.id.time);
            cache.days = (TextView) view.findViewById(R.id.days);
            cache.enabled = (Switch) view.findViewById(R.id.enabled);
            view.setTag(cache);
            return cache;
        }
    }

    private static class AlarmEnabler implements CompoundButton.OnCheckedChangeListener {

        private final Alarm alarm;
        private final Context context;

        public AlarmEnabler(Context context, Alarm alarm) {
            this.context = context;
            this.alarm = alarm;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            alarm.setEnabled(isChecked);
            Alarms.saveAlarm(context, alarm);
        }
    }
}
