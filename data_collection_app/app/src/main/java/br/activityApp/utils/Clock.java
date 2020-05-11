package br.activityApp.utils;


import android.text.format.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Clock {

    public static String getTimestamp() {
        Time now = new Time();
        now.setToNow();
        return now.format("%Y-%m-%d___%H-%M-%S");
    }

    public static Long fromTimestamp(String timestamp) {
        Long startDate = 0L;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
            Date date = sdf.parse(timestamp);

            startDate = date.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return startDate;
    }
}
