package br.activityApp.sensor;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import br.activityApp.data.remote.ApiConfiguration;
import br.activityApp.data.remote.GaitService;
import br.activityApp.data.remote.GaitServiceFactory;
import br.activityApp.utils.SoundPlayer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SensorService extends Service {

    public final static String TYPE_CALIBRATION = "calibration";

    public static final String SERVICE_TYPE = "SERVICE_TYPE";
    public static final String TARGET_NAME = "TARGET_NAME";
    public static final String CALIBRATION_PROGRESS = "CALIBRATION_PROGRESS";
    public static final String CALIBRATION_STATUS = "CALIBRATION_STATUS";
    public static final String CALIBRATION_FINISHED = "CALIBRATION_FINISHED";

    private static final long COUNTDOWN = 10000; // 300 seconds -> 5 minutes
    private static final long TICK_INTERVAL = 1000; // 1 second

    private PowerManager.WakeLock wakeLock;
    private LocalBroadcastManager localBroadcastManager;
    private SensorDataCollector sensorDataCollector = null;
    private CountDownTimer timer = null;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public int onStartCommand(Intent intent, int flags, int startID) {
        String serviceType = intent.getStringExtra(SensorService.SERVICE_TYPE);
        String targetName = intent.getStringExtra(SensorService.TARGET_NAME);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        Notification notification = new Notification.Builder(this).setContentTitle("ActivityApp running").build();
        startForeground(2015, notification);

        PowerManager powerManager;
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ActivityApp WAKE LOCK");
        wakeLock.acquire();

        sensorDataCollector = new SensorDataCollector((SensorManager) getSystemService(Context.SENSOR_SERVICE), serviceType, targetName);
        startCalibration();

        return START_REDELIVER_INTENT;
    }


    @Override
    public void onCreate() {
        Log.d("ActivityApp", "service created");
        Log.d("ActivityApp", "service thread id: " + String.valueOf(android.os.Process.myTid()));
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("ActivityApp", "service destroyed");
        finishCalibration();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateProgress(int progress) {
        Intent intent = new Intent(SensorService.CALIBRATION_PROGRESS);
        intent.putExtra(SensorService.CALIBRATION_PROGRESS, progress);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void updateStatus(String status, boolean hasFinished) {
        Intent intent;
        if (hasFinished) {
            intent = new Intent(SensorService.CALIBRATION_FINISHED);
            intent.putExtra(SensorService.CALIBRATION_STATUS, status);
        } else {
            intent = new Intent(SensorService.CALIBRATION_STATUS);
            intent.putExtra(SensorService.CALIBRATION_STATUS, status);
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    public void startCalibration() {
        Log.d("ActivityApp", "collecting data");
        updateStatus("Collecting data.", false);
        timer = new CountDownTimer(COUNTDOWN, TICK_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                int progress = Math.round((COUNTDOWN - millisUntilFinished) * 100 / COUNTDOWN);
                updateProgress(progress);
            }

            public void onFinish() {
                updateProgress(100);
                //finishCalibration();
            }
        }.start();
    }

    public void finishCalibration() {
        stopThreads();
        SoundPlayer.getInstance().play(SoundPlayer.ALERT_CALIBRATION_FINISHED);

        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(3000);

        updateStatus("Data collection finished.", true);
    }

    private void stopThreads() {
        sensorDataCollector.clean();
        if (timer != null)
            timer.cancel();
        if (wakeLock.isHeld())
            wakeLock.release();
    }
}
