package com.github.arekolek.sarenka.edit;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextPreference2 extends EditTextPreference {

    public EditTextPreference2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        Handler delayedRun = new Handler();
        delayedRun.post(new Runnable() {
            @Override
            public void run() {
                EditText textBox = getEditText();
                textBox.setSelection(0, textBox.getText().length());
            }
        });
    }

    public void show() {
        showDialog(null);
    }
}
