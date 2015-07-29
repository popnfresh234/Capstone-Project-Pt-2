package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by lenovo on 7/29/2015.
 */
public class Utilities {
    public static final String EXTRA_STATION_ID = "com.dmtaiwan.alexander.extra.stationid";

    public static String formatTime(String string) {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        String timeString = "";
        try {
            Date date = format.parse(string);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            String hourString = String.valueOf(hour);
            String minuteString = String.valueOf(minute);
            timeString = hourString + ":" + minuteString;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timeString;
    }
}
