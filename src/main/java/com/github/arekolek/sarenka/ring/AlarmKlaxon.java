package com.github.arekolek.sarenka.ring;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import com.github.arekolek.sarenka.Log;
import com.github.arekolek.sarenka.edit.Alarm;

import java.io.IOException;

public class AlarmKlaxon extends Service implements MediaPlayer.OnErrorListener {

    // Default of 10 minutes until alarm is silenced.
    private static final String DEFAULT_ALARM_TIMEOUT = "10";
    private static final int SKIP_TIMEOUT = (int) (30 * DateUtils.SECOND_IN_MILLIS);

    private boolean mPlaying = false;
    private MediaPlayer mMediaPlayer;
    private Alarm mCurrentAlarm;
    private long mStartTime;
    private TelephonyManager mTelephonyManager;
    private int mInitialCallState;

    // Internal messages
    private static final int KILLER = 1000;
    private static final int SKIPPER = 1001;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:
                    if (Log.LOGV) {
                        Log.v("*********** Alarm killer triggered ***********");
                    }
                    sendKillBroadcast((Alarm) msg.obj, false);
                    stopSelf();
                    break;
                case SKIPPER:
                    Log.v("Alarm skip song");
                    playNextTrack((Alarm) msg.obj);
                    break;
            }
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            if (state != TelephonyManager.CALL_STATE_IDLE
                    && state != mInitialCallState) {
                sendKillBroadcast(mCurrentAlarm, false);
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        // Listen for incoming calls to kill the alarm.
        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(
                mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
    }

    @Override
    public void onDestroy() {
        stop();
        Intent alarmDone = new Intent(Alarms.ALARM_DONE_ACTION);
        sendBroadcast(alarmDone);

        // Stop listening for incoming calls.
        mTelephonyManager.listen(mPhoneStateListener, 0);
        AlarmAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        final Alarm alarm = intent.getParcelableExtra(
                Alarms.ALARM_INTENT_EXTRA);

        if (alarm == null) {
            Log.v("AlarmKlaxon failed to parse the alarm from the intent");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (mCurrentAlarm != null) {
            sendKillBroadcast(mCurrentAlarm, true);
        }

        play(alarm);
        mCurrentAlarm = alarm;
        // Record the initial call state here so that the new alarm has the
        // newest state.
        mInitialCallState = mTelephonyManager.getCallState();

        return START_STICKY;
    }

    private void sendKillBroadcast(Alarm alarm, boolean replaced) {
        long millis = System.currentTimeMillis() - mStartTime;
        int minutes = (int) Math.round(millis / (double) DateUtils.MINUTE_IN_MILLIS);
        Intent alarmKilled = new Intent(Alarms.ALARM_KILLED);
        alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
        alarmKilled.putExtra(Alarms.ALARM_REPLACED, replaced);
        sendBroadcast(alarmKilled);
    }

    private void play(Alarm alarm) {
        // stop() checks to see if we are already playing.
        stop();

        try {
            mMediaPlayer = preparePlayer(alarm);
            startAlarm(mMediaPlayer, alarm.isRandomSound());
        } catch (Exception ex) {
            Log.v("Using the fallback ringtone");
            // The alert may be on the sd card which could be busy right
            // now. Use the fallback ringtone.
            try {
                // Must reset the media player to clear the error state.
                mMediaPlayer.reset();
                // TODO add fallback ringtone
//                setDataSourceFromResource(getResources(), mMediaPlayer,
//                        R.raw.fallbackring);
//                initAlarm(mMediaPlayer, false);
            } catch (Exception ex2) {
                // At this point we just don't play anything.
                Log.e("Failed to play fallback ringtone", ex2);
            }
        }

        if (alarm.isRandomSound()) {
            enableSkipper(alarm);
        }
        enableKiller(alarm);
        mPlaying = true;
        mStartTime = System.currentTimeMillis();
    }

    private void playNextTrack(Alarm alarm) {
        if (Log.LOGV) Log.v("AlarmKlaxon.playNextTrack()");
        try {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = preparePlayer(alarm);
            startAlarm(mMediaPlayer, true);
            enableSkipper(alarm);
        } catch (IOException e) {
            Log.e("Exception playing next track");
        }
    }

    private MediaPlayer preparePlayer(Alarm alarm) throws IOException {
        Uri alert;
        if (alarm.isRandomSound()) {
            alert = AlarmShuffler.getRandomAlarm(this);
        } else {
            alert = alarm.getSoundUri();
        }

        if (Log.LOGV) {
            Log.v("AlarmKlaxon.play() " + alarm.getId() + " alert " + alert);
        }

        // Fall back on the default alarm if the database does not have an
        // alarm stored.
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM);
            if (Log.LOGV) {
                Log.v("Using default alarm: " + alert.toString());
            }
        }

        MediaPlayer player = new MediaPlayer();
        player.setOnErrorListener(this);
        // Check if we are in a call. If we are, use the in-call alarm
        // resource at a low volume to not disrupt the call.
        if (mTelephonyManager.getCallState()
                != TelephonyManager.CALL_STATE_IDLE) {
            // TODO add in call alarm resource
//                    Log.v("Using the in-call alarm");
//                    mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
//                    setDataSourceFromResource(getResources(), mMediaPlayer,
//                            R.raw.in_call_alarm);
        } else {
            player.setDataSource(this, alert);
        }
        return player;
    }

    private void startAlarm(MediaPlayer player, boolean shuffle) throws IOException {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
        // TODO maybe I don't want silent alarms?
        int alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        int systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        if (alarmVolume != 0 && systemVolume != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            int duration = player.getDuration();
            if (shuffle && duration > 2 * SKIP_TIMEOUT) {
                player.seekTo((duration - SKIP_TIMEOUT) / 2);
                player.setLooping(false);
            }
            player.start();
        }
    }

    private void setDataSourceFromResource(Resources resources,
                                           MediaPlayer player, int res) throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    /**
     * Stops alarm audio and disables alarm if it not snoozed and not
     * repeating
     */
    public void stop() {
        if (Log.LOGV) Log.v("AlarmKlaxon.stop()");
        if (mPlaying) {
            mPlaying = false;

            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
        disableKiller();
        disableSkipper();
    }

    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
     * won't run all day.
     * <p/>
     * This just cancels the audio, but leaves the notification
     * popped, so the user will know that the alarm tripped.
     */
    private void enableKiller(Alarm alarm) {
//        final String autoSnooze =
//                PreferenceManager.getDefaultSharedPreferences(this)
//                        .getString(SettingsActivity.KEY_AUTO_SILENCE,
//                                DEFAULT_ALARM_TIMEOUT);
        String autoSnooze = "-1";
        int autoSnoozeMinutes = Integer.parseInt(autoSnooze);
        if (autoSnoozeMinutes != -1) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, alarm),
                    autoSnoozeMinutes * DateUtils.MINUTE_IN_MILLIS);
        }
    }

    private void enableSkipper(Alarm alarm) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(SKIPPER, alarm),
                SKIP_TIMEOUT);
    }

    private void disableKiller() {
        mHandler.removeMessages(KILLER);
    }

    private void disableSkipper() {
        mHandler.removeMessages(SKIPPER);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("Error occurred while playing audio.");
        mp.stop();
        mp.release();
        mMediaPlayer = null;
        return true;
    }
}
