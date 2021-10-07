package com.loussouarn.edouard.go4lunch.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormat {
    public String getTodayDate() {
        Date day = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.FRENCH);
        return f.format(day);
    }

    public String getRegisteredDate(Date myDate) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.FRENCH);
        return  f.format(myDate);
    }

    public String getHoursFormat(String hour) {
        String time;
        if (hour.length()==3) {
            time = hour.substring(0,1) + ":" + hour.substring(1);
        } else {
            time = hour.substring(0,2)+":"+ hour.substring(2);
        }
        return time;
    }
}
