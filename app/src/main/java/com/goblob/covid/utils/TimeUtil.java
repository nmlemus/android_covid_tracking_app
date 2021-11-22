package com.goblob.covid.utils;


import com.goblob.covid.app.CovidApp;
import com.instacart.library.truetime.TrueTime;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by edel on 25/07/17.
 */

public class TimeUtil {

  public static String getUTCTime() {
    Date date;
    if (TrueTime.isInitialized()) {
      date = TrueTime.now();
    } else {
      // GoblobApplication.getInstance().initializeTrueTime();
      date = Calendar.getInstance().getTime();
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    String utcString = sdf.format(date);

    if (utcString == null || utcString.length() == 0){
      sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      utcString = sdf.format(date);
    }

    return utcString;
  }

  public static Date getLocalTime(long time) {
    Calendar calendar = Calendar.getInstance();
    if (TrueTime.isInitialized()) {
      calendar.setTime(TrueTime.now());
    } else {
      // GoblobApplication.getInstance().initializeTrueTime();
    }
    int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);

    Calendar utcCalendar = Calendar.getInstance(Locale.ENGLISH);
    utcCalendar.setTimeInMillis(time);

    long utcTimeInMillis = utcCalendar.getTimeInMillis() + offset;

    return new Date(utcTimeInMillis);
  }

  public static long getLocalTime() {
    Date date;
    if (TrueTime.isInitialized()) {
      date = TrueTime.now();
    } else {
      CovidApp.getInstance().initializeTrueTime();
      date = Calendar.getInstance().getTime();
    }
    return date.getTime();
  }

  public static Date getUTCTime(String utcString) {
    SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    Date startDateTimeUTC = utcFormat.parse(utcString, new ParsePosition(0));

    if (startDateTimeUTC == null){
      utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      startDateTimeUTC = utcFormat.parse(utcString, new ParsePosition(0));
    }

    return startDateTimeUTC;
  }

  public static long getUTCTimeInMillis(String utcString) {
    SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    Date startDateTimeUTC = utcFormat.parse(utcString, new ParsePosition(0));

    if (startDateTimeUTC == null){
      utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      startDateTimeUTC = utcFormat.parse(utcString, new ParsePosition(0));
    }

    long l = startDateTimeUTC.getTime();

    return l;
  }

  public static Date getLocalTime(String utcString) {
    return getLocalTime(getUTCTime(utcString).getTime());
  }

  public static String getLocalTimeString(String utcString) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    sdf.setTimeZone(TimeZone.getDefault());
    Date date = getLocalTime(getUTCTime(utcString).getTime());
    String s = sdf.format(date);

    if (s == null || s.length() == 0){
      sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      sdf.setTimeZone(TimeZone.getDefault());
      date = getLocalTime(getUTCTime(utcString).getTime());
      s = sdf.format(date);
    }

    return s;
  }

  public static String getLocalTimeString(Date utcTime) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    sdf.setTimeZone(TimeZone.getDefault());
    Date date = getLocalTime(utcTime.getTime());
    String s = sdf.format(date);

    if (s == null || s.length() == 0){
      sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      sdf.setTimeZone(TimeZone.getDefault());
      date = getLocalTime(utcTime.getTime());
      s = sdf.format(date);
    }

    return s;
  }

  public static String getLocalTimeString() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    sdf.setTimeZone(TimeZone.getDefault());
    String s = sdf.format(getLocalTime());

    if (s == null || s.length() == 0){
      sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      sdf.setTimeZone(TimeZone.getDefault());
      s = sdf.format(getLocalTime());
    }

    return s;
  }

  public static long getLocalTimeInMillis(String utcString) {
    return getLocalTime(getUTCTime(utcString).getTime()).getTime();
  }

  public static long getUTCTimeInMillis() {
    return getUTCTime(getUTCTime()).getTime();
  }

  public static String getUTCTime(Date localTime) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    String utcString = sdf.format(localTime);

    if (utcString == null || utcString.length() == 0){
      sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      utcString = sdf.format(localTime);
    }

    return utcString;
  }

  public static boolean isGreaterThan(String date1, String date2) {
    SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    Date d1 = utcFormat.parse(date1, new ParsePosition(0));

    if (d1 == null){
      utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      d1 = utcFormat.parse(date1, new ParsePosition(0));
    }

    utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    Date d2 = utcFormat.parse(date2, new ParsePosition(0));

    if (d2 == null){
      utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
      d2 = utcFormat.parse(date2, new ParsePosition(0));
    }

    return d1.getTime() > d2.getTime();
  }
}
