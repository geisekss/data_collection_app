package br.activityApp.utils;

import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import br.activityApp.CalibrateActivity;


public class DataProcessor {

    public static final String FILE_DATA_ACCELEROMETER = "data_accelerometer.txt";
    public static final String FILE_DATA_ROTATION_VECTOR = "data_rotation_vector.txt";
    public static final String FILE_DATA_MAGNETIC_FIELD = "data_magnetic_field.txt";
    public static final String FILE_DATA_GYROSCOPE = "data_gyroscope.txt";

//    public static void saveUserData() {
//        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsoluteFile();
//        String targetName = CalibrateActivity.getTargetName() + "___" + Clock.getTimestamp() + "___";
//
//        File fileDataAccelerometer = new File(path, FILE_DATA_ACCELEROMETER);
//        File fileDataRotationVector = new File(path, FILE_DATA_ROTATION_VECTOR);
//        File fileDataMagneticField = new File(path, FILE_DATA_MAGNETIC_FIELD);
//        File fileDataGyroscope = new File(path, FILE_DATA_GYROSCOPE);
//
//        File copyDataAccelerometer = new File(path, targetName + FILE_DATA_ACCELEROMETER);
//        File copyDataRotationVector = new File(path, targetName + FILE_DATA_ROTATION_VECTOR);
//        File copyDataMagneticField = new File(path, targetName + FILE_DATA_MAGNETIC_FIELD);
//        File copyDataGyroscope = new File(path, targetName + FILE_DATA_GYROSCOPE);
//
//        copyFile(fileDataAccelerometer, copyDataAccelerometer);
//        copyFile(fileDataRotationVector, copyDataRotationVector);
//        copyFile(fileDataMagneticField, copyDataMagneticField);
//        copyFile(fileDataGyroscope, copyDataGyroscope);
//    }

    public static void writeFile(FileHandler file, ArrayList<Pair<Long[], Float[]>> data, boolean addTrailingZero) {
        Log.d("LOG", "writing to file");
        for (Pair<Long[], Float[]> row : data) {
            String output = "";

            // write timestamp
            Long[] times = row.first;
            for (Long t : times)
                output += "," + String.valueOf(t);
            output = output.substring(1);
            // write values
            Float[] values = row.second;
            for (Float f : values)
                output += "," + String.valueOf(f);

            if (addTrailingZero)
                output += ",0\n";
            else
                output += "\n";

            file.writeToFile(output);

        }
        //file.closeFile();
    }

    private static void copyFile(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            src.delete();
        } catch (IOException e) {
            Log.e("ActivityApp", "problem copying file: " + e.toString());
        }
    }
}
