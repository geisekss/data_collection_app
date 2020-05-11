package br.activityApp.data.local;

import br.activityApp.utils.Clock;

public class FileItem {

    private static String SEPARATOR_FIELDS = "__";

    private String filename;
    private String name;
    private String date;
    private String time;
    private String sensor;
    private boolean synced;

    /**
     * This constructor is used from creating a new instance of FileItem object from the file name
     *
     * @param filename the file name to be parsed
     * @return the FileItem instance object
     */
    public static FileItem fromFilename(String filename) {
        String[] splitted = filename.split(SEPARATOR_FIELDS);

        String name = splitted[0];
        String date = cleanDate(splitted[1]);
        String time = cleanTime(splitted[2]);
        String sensor = splitted[3];

        return new FileItem(filename, name, date, time, sensor);
    }

    public FileItem(String filename, String name, String date, String time, String sensor) {
        this.filename = filename;
        this.name = name;
        this.date = date;
        this.time = time;
        this.sensor = sensor;
    }

    public static String cleanDate(String dirtyDate) {
        return dirtyDate.replace("-", "/").replace("_", "");
    }

    public static String cleanTime(String dirtyTime) {
        return dirtyTime.replace("-", ":").replace("_", "");
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isSynced(Long lastSync) {
        Long creationDate = Clock.fromTimestamp(getDate());

        return creationDate < lastSync;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getPrettyDate() {
        return getDate() + " (" + getDate() + ")";
    }
}
