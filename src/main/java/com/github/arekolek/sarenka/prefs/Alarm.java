package com.github.arekolek.sarenka.prefs;

import android.content.Context;

import com.orm.SugarRecord;

public class Alarm extends SugarRecord<Alarm> {

    Integer hour;
    Integer minute;
    Integer days;
    Boolean enabled;
    String label;
    String sound;

    public Alarm(Context context) {
        super(context);
    }

}
