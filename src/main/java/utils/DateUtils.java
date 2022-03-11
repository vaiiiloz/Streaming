package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateUtils {
    private static final String[] DAY_IN_WEEK = new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};

    public DateUtils(){

    }

    public static long getCurrentTime(){
        Calendar currentCal = Calendar.getInstance();
        return currentCal.getTimeInMillis();
    }

    public static Date createDate(int year, int month, int dayOfMonth, int hour, int minute, int second){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, dayOfMonth, hour, minute, second);
        return calendar.getTime();
    }

    public static Date getDateByMilliseconds(long milliseconds){
        return formatDateByMillisecondsWithFormat(milliseconds, "HH:mm");
    }

    public static Date formatCurrentDate(String formatDate){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(formatDate);

        try{
            return sdf.parse(sdf.format(calendar.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public static Date formatDateWithFormat(Date date, String formatDate){
        SimpleDateFormat sdf = new SimpleDateFormat(formatDate);
        try{
            date = sdf.parse(formatDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }

    }

    public static Date formatDateByMillisecondsWithFormat(long milliseconds, String formatDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatDateWithFormat(calendar.getTime(), formatDate);
    }

    public static String getTimeStringNow(String formatDate){
        DateFormat sdf;
        sdf = new SimpleDateFormat(formatDate);
        Calendar currentCal = Calendar.getInstance();
        Date netDate = new Date(currentCal.getTimeInMillis());
        String time = sdf.format(netDate);
        return time;
    }
}
