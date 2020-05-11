package br.activityApp.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import br.activityApp.utils.Clock;
import br.activityApp.utils.DataProcessor;
import br.activityApp.utils.FileHandler;

/**
 * This class is used to track the accelerometer sensor changes, as well as persist any received
 * information into the disk.
 */
public class SensorListenerAccelerometer implements SensorEventListener {

    // threshold (in milliseconds) to force a write into the disk
    private static long THRESHOLD_WRITE = 5000L;

    private ArrayList<Pair<Long[], Float[]>> data;

    protected FileHandler fileAccelerometer;

    private long initialTime;

    public SensorListenerAccelerometer(ArrayList<Pair<Long[], Float[]>> data, String targetName) {
        super();

        this.data = data;
        this.initialTime = System.currentTimeMillis();

        String fileName = targetName + "___" + Clock.getTimestamp() + "___" + DataProcessor.FILE_DATA_ACCELEROMETER;
        Log.d("LOG", ">>> filename: " + fileName);
        fileAccelerometer = new FileHandler(fileName);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }


    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];

            data.add(new Pair<>(new Long[]{event.timestamp, System.currentTimeMillis()}, new Float[]{x, y, z}));

            // check if time since last write has passed
            if (exceededTime()) {
                DataProcessor.writeFile(fileAccelerometer, data, false);
                data = new ArrayList<>();

                // reset timer
                initialTime = System.currentTimeMillis();
                // TODO may we send data to the server here?..
                Log.d("LOG", ">>> new data saved in the disk!");
            }
        }
    }

    private boolean exceededTime() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - initialTime) > THRESHOLD_WRITE;
    }

    public final void finishListener() {
        data = null;
    }
}