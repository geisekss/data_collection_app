package br.activityApp.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import br.activityApp.utils.DataProcessor;
import br.activityApp.utils.FileHandler;

public class SensorDataCollector {

    private SensorManager sensorManager;
    private String serviceType;
    private String targetName;

    // min sampling periods for Nexus 4
    public final static int SENSOR_DELAY_ACCELEROMETER = 10000; // min = 5000
    public final static int SENSOR_DELAY_ROTATION_VECTOR = 10000; // min = 5000
    public final static int SENSOR_DELAY_MAGNETIC_FIELD = 10000; // min = 5000
    public final static int SENSOR_DELAY_GYROSCOPE = 10000; // min = 5000

    private SensorListenerAccelerometer eventListenerAccelerometer;
    private SensorListenerRotationVector eventListenerRotationVector;
//    private SensorListenerMagneticField eventListenerMagneticField;
//    private SensorListenerGyroscope eventListenerGyroscope;

    private ArrayList<Pair<Long[], Float[]>> dataAccelerometer;
    private ArrayList<Pair<Long[], Float[]>> dataRotationVector;
//    private ArrayList<Pair<Long[], Float[]>> dataMagneticField;
//    private ArrayList<Pair<Long[], Float[]>> dataGyroscope;

    private SensorWorkerAccelerometer sensorWorkerAccelerometer;
    private SensorWorkerRotationVector sensorWorkerRotationVector;
//    private SensorWorkerMagneticField sensorWorkerMagneticField;
//    private SensorWorkerGyroscope sensorWorkerGyroscope;

    private Thread threadAccelerometer;
    private Thread threadRotationVector;
//    private Thread threadMagneticField;
//    private Thread threadGyroscope;

    public SensorDataCollector(SensorManager sensorManager, String serviceType, String targetName) {
        this.sensorManager = sensorManager;
        this.serviceType = serviceType;
        this.targetName = targetName;
        initialize();
    }

    private void initialize() {
        Sensor sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        logSensorDetails(sensorAccelerometer);
        logSensorDetails(sensorRotationVector);
        logSensorDetails(sensorMagneticField);
        logSensorDetails(sensorGyroscope);

        sensorWorkerAccelerometer = new SensorWorkerAccelerometer();
        sensorWorkerRotationVector = new SensorWorkerRotationVector();
//        sensorWorkerMagneticField = new SensorWorkerMagneticField();
//        sensorWorkerGyroscope = new SensorWorkerGyroscope();

        threadAccelerometer = new Thread(sensorWorkerAccelerometer);
        threadRotationVector = new Thread(sensorWorkerRotationVector);
//        threadMagneticField = new Thread(sensorWorkerMagneticField);
//        threadGyroscope = new Thread(sensorWorkerGyroscope);

        threadAccelerometer.start();
        threadRotationVector.start();
//        threadMagneticField.start();
//        threadGyroscope.start();
    }

    public void clean() {
        long initialTime = System.currentTimeMillis();
        try {
            if(threadAccelerometer.isAlive()) {
                Log.d("ActivityApp", "sensor thread accelerometer stopping");
                sensorWorkerAccelerometer.getHandler().getLooper().quitSafely();
                threadAccelerometer.join();
                sensorManager.unregisterListener(eventListenerAccelerometer);
                if(serviceType.equals(SensorService.TYPE_CALIBRATION))
                    eventListenerAccelerometer.finishListener();

            }
//            Log.d("LOG", "current time for accelerometer: " + String.valueOf(System.currentTimeMillis() - initialTime));
//            initialTime = System.currentTimeMillis();
//            if(threadRotationVector.isAlive()) {
//                Log.d("ActivityApp", "sensor thread rotation vector stopping");
//                sensorWorkerRotationVector.getHandler().getLooper().quitSafely();
//                threadRotationVector.join();
//                sensorManager.unregisterListener(eventListenerRotationVector);
//                if(serviceType.equals(SensorService.TYPE_CALIBRATION))
//                    eventListenerRotationVector.finishListener();
//            }
//            Log.d("LOG", "current time for rotation vector: " + String.valueOf(System.currentTimeMillis() - initialTime));
//            initialTime = System.currentTimeMillis();
//            if(threadMagneticField.isAlive()) {
//                Log.d("ActivityApp", "sensor thread magnetic field stopping");
//                sensorWorkerMagneticField.getHandler().getLooper().quitSafely();
//                threadMagneticField.join();
//                sensorManager.unregisterListener(eventListenerMagneticField);
//                if(serviceType.equals(SensorService.TYPE_CALIBRATION))
//                    eventListenerMagneticField.finishListener();
//            }
//            Log.d("LOG", "current time for magnetic field: " + String.valueOf(System.currentTimeMillis() - initialTime));
//            initialTime = System.currentTimeMillis();
//            if(threadGyroscope.isAlive()) {
//                Log.d("ActivityApp", "sensor thread gyroscope stopping");
//                sensorWorkerGyroscope.getHandler().getLooper().quitSafely();
//                threadGyroscope.join();
//                sensorManager.unregisterListener(eventListenerGyroscope);
//                if(serviceType.equals(SensorService.TYPE_CALIBRATION))
//                    eventListenerGyroscope.finishListener();
//            }
//            Log.d("LOG", "current time for gyroscope: " + String.valueOf(System.currentTimeMillis() - initialTime));
        } catch (InterruptedException e) {
            Log.e("ActivityApp", "authenticator thread exception: " + e.toString());
        }
    }

    private void logSensorDetails(Sensor sensor) {
        try {
            Log.d("ActivityApp", sensor.getName() + "\n" +
                    sensor.getStringType() + "\n" +
                    sensor.getVendor() + "\n" +
                    sensor.getVersion() + "\n" +
                    sensor.getFifoMaxEventCount() + "\n" +
                    sensor.getFifoReservedEventCount() + "\n" +
                    sensor.getMaxDelay() + "\n" +
                    sensor.getMinDelay() + "\n" +
                    sensor.getMaximumRange() + "\n" +
                    sensor.getPower() + "\n" +
                    sensor.getResolution() + "\n" +
                    sensor.getReportingMode() + "\n"
            );
        } catch (Exception e) {
            Log.e("LOG", "logSensorDetails: " + e.toString());
        }
    }



    private class SensorWorkerAccelerometer implements Runnable {

        private Handler handler;

        @Override
        public void run() {
            Log.d("ActivityApp", "sensor thread accelerometer running");
            Log.d("ActivityApp", "sensor thread id: " + String.valueOf(android.os.Process.myTid()));

            Looper.prepare();
            handler = new Handler();

            dataAccelerometer = new ArrayList<>();
            eventListenerAccelerometer = new SensorListenerAccelerometer(dataAccelerometer, targetName);

            sensorManager.registerListener(eventListenerAccelerometer, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY_ACCELEROMETER, handler);
            Looper.loop();
        }

        public Handler getHandler() {
            return handler;
        }
    }

    private class SensorWorkerRotationVector implements Runnable {

        private Handler handler;

        @Override
        public void run() {
            Log.d("ActivityApp", "sensor thread rotatiotorn vec running");
            Log.d("ActivityApp", "sensor thread id: " + String.valueOf(android.os.Process.myTid()));

            Looper.prepare();
            handler = new Handler();

            dataRotationVector = new ArrayList<>();
            eventListenerRotationVector = new SensorListenerRotationVector(dataRotationVector, targetName);

            sensorManager.registerListener(eventListenerRotationVector, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SENSOR_DELAY_ROTATION_VECTOR, handler);
            Looper.loop();
        }

        public Handler getHandler() {
            return handler;
        }
    }

//    private class SensorWorkerRotationVector implements Runnable {
//
//        private Handler handler;
//
//        @Override
//        public void run() {
//            Log.d("ActivityApp", "sensor thread rotation vector running");
//            Log.d("ActivityApp", "sensor thread id: " + String.valueOf(android.os.Process.myTid()));
//
//            Looper.prepare();
//            handler = new Handler();
//
//            dataRotationVector = new ArrayList<>();
//            eventListenerRotationVector = new SensorListenerRotationVector(dataRotationVector);
//
//            sensorManager.registerListener(eventListenerRotationVector, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SENSOR_DELAY_ROTATION_VECTOR, handler);
//            Looper.loop();
//        }
//
//        public Handler getHandler() {
//            return handler;
//        }
//    }
//
//    private class SensorWorkerMagneticField implements Runnable {
//
//        private Handler handler;
//
//        @Override
//        public void run() {
//            Log.d("ActivityApp", "sensor thread magnetic field running");
//            Log.d("ActivityApp", "sensor thread id: " + String.valueOf(android.os.Process.myTid()));
//
//            Looper.prepare();
//            handler = new Handler();
//
//            if (serviceType.equals(SensorService.TYPE_CALIBRATION)) {
//                dataMagneticField = new ArrayList<>();
//                eventListenerMagneticField = new SensorListenerMagneticField(dataMagneticField);
//            }
//
//            sensorManager.registerListener(eventListenerMagneticField, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SENSOR_DELAY_MAGNETIC_FIELD, handler);
//            Looper.loop();
//        }
//
//        public Handler getHandler() {
//            return handler;
//        }
//    }
//
//    private class SensorWorkerGyroscope implements Runnable {
//
//        private Handler handler;
//
//        @Override
//        public void run() {
//            Log.d("ActivityApp", "sensor thread gyroscope running");
//            Log.d("ActivityApp", "sensor thread id: " + String.valueOf(android.os.Process.myTid()));
//
//            Looper.prepare();
//            handler = new Handler();
//
//            dataGyroscope = new ArrayList<>();
//            eventListenerGyroscope = new SensorListenerGyroscope(dataGyroscope);
//
//            sensorManager.registerListener(eventListenerGyroscope, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SENSOR_DELAY_GYROSCOPE, handler);
//            Looper.loop();
//        }
//
//        public Handler getHandler() {
//            return handler;
//        }
//    }
}
