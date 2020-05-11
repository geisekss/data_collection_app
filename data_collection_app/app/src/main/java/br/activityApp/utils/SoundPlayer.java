package br.activityApp.utils;

import android.content.Context;
import android.media.SoundPool;

import br.activityApp.R;

public class SoundPlayer {

    public static final int ALERT_NOT_WALKING = 1;
    public static final int ALERT_AUTHENTICATION_GOOD = 2;
    public static final int ALERT_AUTHENTICATION_FAILED = 3;
    public static final int ALERT_CALIBRATION_FINISHED = 4;

    private static SoundPlayer instance = new SoundPlayer();
    private static SoundPool soundPool = new SoundPool.Builder().build();

    private int alertNotWalking;
    private int alertAuthenticationGood;
    private int alertAuthenticationFailed;

    private SoundPlayer() {}

    public static SoundPlayer getInstance() {
        return instance;
    }

    public void initialize(Context context) {
        alertNotWalking = soundPool.load(context, R.raw.alert_not_walkin, 1);
        alertAuthenticationGood = soundPool.load(context, R.raw.alert_authentication_good, 1);
        alertAuthenticationFailed = soundPool.load(context, R.raw.alert_authentication_failed, 1);
    }

    public void play(int value) {
        switch (value) {
            case ALERT_NOT_WALKING:
                soundPool.play(alertNotWalking, 1, 1, 0, 0, 1);
                break;
            case ALERT_AUTHENTICATION_FAILED:
                soundPool.play(alertAuthenticationFailed, 1, 1, 0, 0, 1);
                break;
            case ALERT_AUTHENTICATION_GOOD:
                soundPool.play(alertAuthenticationGood, 1, 1, 0, 0, 1);
                break;
            case ALERT_CALIBRATION_FINISHED:
                soundPool.play(alertAuthenticationGood, 1, 1, 0, 0, 1);
                break;
        }
    }
}
