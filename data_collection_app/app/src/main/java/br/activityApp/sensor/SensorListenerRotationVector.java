package br.activityApp.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import br.activityApp.utils.Clock;
import br.activityApp.utils.DataProcessor;
import br.activityApp.utils.FileHandler;

public class SensorListenerRotationVector implements SensorEventListener {

    private static long THRESHOLD_WRITE = 5000L;

    private ArrayList<Pair<Long[], Float[]>> data;

    protected FileHandler fileRotationVector;

    private long initialTime;

    public SensorListenerRotationVector(ArrayList<Pair<Long[], Float[]>> data, String targetName) {
        super();

        this.data = data;
        this.initialTime = System.currentTimeMillis();

        String fileName = targetName + "___" + Clock.getTimestamp() + "___" + DataProcessor.FILE_DATA_ROTATION_VECTOR;
        Log.d("LOG", ">>> filename: " + fileName);
        fileRotationVector = new FileHandler(fileName);
    }
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Long timestamp = System.currentTimeMillis();
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];
            Float cos = event.values[3];
            Float acc = event.values[4];

            data.add(new Pair<>(new Long[] {event.timestamp, System.currentTimeMillis()}, new Float[]{x, y, z, cos, acc}));

            if (exceededTime()) {
                DataProcessor.writeFile(fileRotationVector, data, false);
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


