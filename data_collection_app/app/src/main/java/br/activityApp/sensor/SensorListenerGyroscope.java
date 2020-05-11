package br.activityApp.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import br.activityApp.utils.DataProcessor;
import br.activityApp.utils.FileHandler;

public class SensorListenerGyroscope implements SensorEventListener {

    private ArrayList<Pair<Long[], Float[]>> data = null;
    private ArrayList<Pair<Long[], Float[]>> dataCopy = null;
    protected FileHandler fileGyroscope;
    private SensorWorkerSaveData sensorWorkerSaveData;
    private Thread threadSaveData;
    private int bufferSize = 80000;


    public SensorListenerGyroscope(ArrayList<Pair<Long[], Float[]>> data) {
        super();
        this.data = data;
        this.dataCopy = new ArrayList<>();
        fileGyroscope = new FileHandler(DataProcessor.FILE_DATA_GYROSCOPE);
        sensorWorkerSaveData = new SensorWorkerSaveData();
        threadSaveData = new Thread(sensorWorkerSaveData);
        threadSaveData.start();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];

            if(data != null)
                data.add(new Pair<>(new Long[] {event.timestamp, System.currentTimeMillis()}, new Float[]{x, y, z}));

            if(data.size() > bufferSize) {
                dataCopy = data;
                data = new ArrayList<>();
            }
        }
    }

    public final void finishListener(){
        data = null;
        if(threadSaveData.isAlive()){
            Log.d("ActivityApp", "thread save data stopping");
            try {
                sensorWorkerSaveData.finish();
                threadSaveData.join();
            } catch (InterruptedException e) {
                Log.e("ActivityApp", "authenticator thread exception: " + e.toString());
            }
        }
    }



    public class SensorWorkerSaveData implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            Log.d("ActivityApp", "save gyroscope data thread running");
            Log.d("ActivityApp", "thread id: " + String.valueOf(android.os.Process.myTid()));
            while (running) {
                if (dataCopy.size() >= bufferSize) {
                    DataProcessor.writeFile(fileGyroscope, dataCopy, false);
                    dataCopy = new ArrayList<>();
                }
//                try {
//                    Thread.sleep(bufferSize);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    running = false;
//                }
            }
            fileGyroscope.closeFile();

        }

        public void finish() {
            running = false;
        }
    }
}