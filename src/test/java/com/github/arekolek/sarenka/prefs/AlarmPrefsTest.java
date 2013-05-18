package com.github.arekolek.sarenka.prefs;

import android.app.Activity;
import android.content.Context;
import com.github.arekolek.junitparams.RobolectricParameterised;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junitparams.JUnitParamsRunner.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(RobolectricParameterised.class)
public class AlarmPrefsTest {

    private AlarmPrefs prefs;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        //prefs = new AlarmPrefs(context);
    }

    @Test
    public void testGetDays() {
        //assertThat(days, arrayContaining(both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(7))));
        //assertThat(days, both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(7)));
    }

    @Test
    @Parameters({
            "17, false",
            "22, true"})
    public void personIsAdult(int age, boolean valid) throws Exception {
        assertThat(new Person(age).isAdult(), is(valid));
    }

    private Object[] values() {
        return $(
                1, 2, 3
        );
    }

    public class Person {
        private int age;

        public Person(int age) {
            this.age = age;
        }

        public boolean isAdult() {
            return age >= 18;
        }

        @Override
        public String toString() {
            return "Person of age: " + age;
        }
    }

}
