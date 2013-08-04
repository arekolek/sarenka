package com.github.arekolek.sarenka.ring;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlarmShuffler {

    private static final long MIN_DURATION = 1000 * 2;
    private static final Uri INTERNAL_MEDIA = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
    private static final Uri EXTERNAL_MEDIA = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION = new String[]{MediaStore.Audio.Media._ID};
    private static final String SELECTION = "(" + MediaStore.Audio.Media.IS_ALARM + " != 0 OR "
            + MediaStore.Audio.Media.IS_MUSIC + " != 0) AND "
            + MediaStore.Audio.Media.DURATION + " > " + MIN_DURATION;

    public static Uri getRandomAlarm(Context context) {
        List<Uri> uris = getAlarmsAndMusic(context);
        return uris.get(getRandom(uris.size()));
    }

    // get random in [0..n)
    private static int getRandom(int n) {
        Random random = new Random();
        return random.nextInt(n);
    }

    private static List<Uri> getAlarmsAndMusic(Context context) {
        List<Uri> list = getAlarmsAndMusic(context, INTERNAL_MEDIA);
        list.addAll(getAlarmsAndMusic(context, EXTERNAL_MEDIA));
        return list;
    }

    private static List<Uri> getAlarmsAndMusic(Context context, Uri uri) {
        Cursor c = context.getContentResolver().query(uri, PROJECTION, SELECTION, null, null);
        List<Uri> uris = new ArrayList<Uri>();
        if (c != null) {
            while (c.moveToNext()) {
                uris.add(ContentUris.withAppendedId(uri, c.getInt(0)));
            }
            c.close();
        }
        return uris;
    }

}
