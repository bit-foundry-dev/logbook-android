package com.bit.logbook.core.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String formatFullDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        return sdf.format(calendar.getTime());
    }

    public static String formatFullDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        return sdf.format(date);
    }

    public static String formatFullDate(LocalDate localDate) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
        return localDate.format(dtf);
    }

    public static String formatShortDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return sdf.format(calendar.getTime());
    }

    public static String formatMonthYear(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        return sdf.format(calendar.getTime());
    }

    public static String formatTime(LocalDateTime localDateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        return dtf.format(localDateTime);
    }
}