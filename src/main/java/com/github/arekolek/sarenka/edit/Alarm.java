package com.github.arekolek.sarenka.edit;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.github.arekolek.sarenka.Ints;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.*;

import static com.orm.SugarApp.*;

public class Alarm extends SugarRecord<Alarm> implements Parcelable {

    public static final Parcelable.Creator<Alarm> CREATOR
            = new Parcelable.Creator<Alarm>() {
        public Alarm createFromParcel(Parcel p) {
            return new Alarm(p);
        }

        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeLong(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minute);
        p.writeInt(days);
        p.writeLong(time);
        p.writeString(label);
        p.writeString(sound);
    }

    public boolean enabled;
    public int hour;
    public int minute;
    public int days;
    @Ignore
    public long time;
    public String label;
    public String sound;
    public String barcode;
    public String barcodeHint;

    public Alarm(Parcel p) {
        super(getSugarContext());
        id = p.readLong();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minute = p.readInt();
        days = p.readInt();
        time = p.readLong();
        label = p.readString();
        sound = p.readString();
    }

    public Alarm(Context context) {
        super(context);
    }

    public String getFormattedTime(Context context) {
        TimeCalendar calendar = new TimeCalendar();
        calendar.setHour(hour);
        calendar.setMinute(minute);
        return calendar.getTimeString(context);
    }

    public String getLabelOrDefault(Context context) {
        return TextUtils.isEmpty(label) ? "ALARMIK" : label;
    }

    public String getFormattedLabel() {
        return TextUtils.isEmpty(label) ? null : days == 0 ? label : label + ": ";
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFormattedDays() {
        DayCalendar calendar = new DayCalendar();
        List<String> dayNames = calendar.convertDaysToNames(getDays());
        return TextUtils.join(", ", dayNames);
    }

    public void setDays(SortedSet<Integer> days) {
        this.days = 0;
        for (Integer day : days) {
            this.days |= 1 << day;
        }
    }

    public DaysOfWeek getDaysOfWeek() {
        return new DaysOfWeek(this.days);
    }

    public SortedSet<Integer> getDays() {
        TreeSet<Integer> days = new TreeSet<Integer>();
        for (int i = 1; i < 8; ++i) {
            if ((this.days & (1 << i)) != 0) {
                days.add(i);
            }
        }
        return days;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSound() {
        return sound;
    }

    public Uri getSoundUri() {
        return TextUtils.isEmpty(sound) ? null : Uri.parse(sound);
    }

    @Override
    public int hashCode() {
        return Ints.checkedCast(id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Alarm)) return false;
        final Alarm other = (Alarm) o;
        return id == other.id;
    }

    /*
     * Days of week code as a single int.
     * 0x00: no day
     * 0x01: Sunday
     * 0x02: Monday
     * 0x04: Tuesday
     * 0x08: Wednesday
     * 0x10: Thursday
     * 0x20: Friday
     * 0x40: Saturday
     */
    public static final class DaysOfWeek {

        private static int[] DAY_MAP = new int[]{
                Calendar.SUNDAY,
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY
        };


        private static HashMap<Integer, Integer> DAY_TO_BIT_MASK = new HashMap<Integer, Integer>();

        static {
            for (int i = 0; i < DAY_MAP.length; i++) {
                DAY_TO_BIT_MASK.put(DAY_MAP[i], i);
            }
        }

        // Bitmask of all repeating days
        private int mDays;

        DaysOfWeek(int days) {
            mDays = days;
        }

        private boolean isSet(int day) {
            return ((mDays & (1 << day)) > 0);
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }

        /**
         * returns number of days from today until next alarm
         *
         * @param c must be set to today
         */
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int today = c.get(Calendar.DAY_OF_WEEK);

            int day;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }


    }
}
