package com.coder520.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static Calendar calendar=Calendar.getInstance();
    public static int getTodayWeek(){

        calendar.setTime(new Date());
        int week=calendar.get(Calendar.DAY_OF_WEEK)-1;
        if (week==0)week=7;
        return week;
    }
    public static int getMunite(Date startDate,Date endDate){
        long start=startDate.getTime();
        long end=endDate.getTime();
        int minute=(int)(end-start)/(1000*60);
        return minute;
    }
    public static Date getDate(int hour,int minute){
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        return calendar.getTime();
    }
}
