package br.activityApp.utils;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.activityApp.data.local.FileItem;

public class FileHandler {

    private static String DEFAULT_FOLDER_ARTIFACTS = "gait-data";

    private File file;
    private BufferedOutputStream out;
    private FileOutputStream fos;

    public FileHandler(String filename) {
        filename = filename.replace(' ', '_');
        if (isExternalStorageWritable()) {
            try {
                file = getFile(filename);
                out = getOutput();
                fos = new FileOutputStream(file, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static File getFile(String name) {
        File path = new File(Environment.getExternalStorageDirectory(), DEFAULT_FOLDER_ARTIFACTS);

        File file = new File(path, name);
        try {
            path.mkdirs();
            file.createNewFile();
            Log.d("SMARTID", "using file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("SMARTID", "file creation failed: " + e.toString());
        }
        return file;
    }


    public static List<FileItem> getAllFiles() {
        File root = new File(Environment.getExternalStorageDirectory(), DEFAULT_FOLDER_ARTIFACTS);
        File[] files = root.listFiles();

        List<FileItem> fileItems = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            FileItem fileItem = FileItem.fromFilename(files[i].getName());
            fileItems.add(fileItem);
        }

        return fileItems;
    }

    public static List<FileItem> getUnsyncedFiles(Long lastSync) {
        List<FileItem> allFiles = getAllFiles();

        for (FileItem file : allFiles) {
            if (file.isSynced(lastSync)) {
                allFiles.remove(file);
            }
        }

        return allFiles;
    }

    private BufferedOutputStream getOutput() {
        try {
            return new BufferedOutputStream(new FileOutputStream(file));
        } catch (IOException e) {
            Log.e("SMARTID", "file buffer creation failed: " + e.toString());
        }
        return null;
    }

    public void writeToFile(String data) {
        try {
            Log.d("SMARTID", "file write");
            fos.write(data.getBytes());
        } catch (IOException e) {
            Log.e("SMARTID", "file write failed: " + e.toString());
        }
    }

    public void closeFile() {
        try {
            out.flush();
            out.close();
            fos.close();
        } catch (IOException e) {
            Log.e("SMARTID", "file close failed: " + e.toString());
        }
    }

}
