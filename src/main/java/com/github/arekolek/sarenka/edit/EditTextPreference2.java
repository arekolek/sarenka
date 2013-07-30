package com.github.arekolek.sarenka.edit;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class EditTextPreference2 extends EditTextPreference {

    public EditTextPreference2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void show() {
        showDialog(null);
    }
}
