
package com.github.arekolek.sarenka.edit;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import com.github.arekolek.sarenka.R;

public class TimePickerPreference extends DialogPreference {

    public static final String DEFAULT_VALUE = "12:00";
    private TimePicker picker;
    private TimeCalendar calendar = new TimeCalendar();

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        picker = (TimePicker) view.findViewById(R.id.time);
        picker.setIs24HourView(true);
        return view;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker.setCurrentHour(calendar.getHour());
        picker.setCurrentMinute(calendar.getMinute());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            calendar.setHour(picker.getCurrentHour());
            calendar.setMinute(picker.getCurrentMinute());
            String currentTime = calendar.getTimeString(getContext());
            persistString(currentTime);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            calendar.fromTimeString(getPersistedString(DEFAULT_VALUE));
        } else {
            calendar.fromTimeString((String) defaultValue);
            persistString(calendar.getTimeString(getContext()));
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

}
