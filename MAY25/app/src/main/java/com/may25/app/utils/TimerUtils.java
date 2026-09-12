package com.may25.app.utils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TimerUtils {

    // Start date: 25 May 2023
    private static final long START_TIME;

    static {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.MAY, 25, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        START_TIME = cal.getTimeInMillis();
    }

    /** Short format for nav drawer: e.g. "1112 days 14:32:08" */
    public static String getElapsedTime() {
        long elapsed = System.currentTimeMillis() - START_TIME;
        long days    = TimeUnit.MILLISECONDS.toDays(elapsed);
        long hours   = TimeUnit.MILLISECONDS.toHours(elapsed) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;
        return String.format("❤️ %d days %02d:%02d:%02d", days, hours, minutes, seconds);
    }

    /** Full format for dialog */
    public static String getElapsedTimeFull() {
        long elapsed  = System.currentTimeMillis() - START_TIME;
        long days     = TimeUnit.MILLISECONDS.toDays(elapsed);
        long hours    = TimeUnit.MILLISECONDS.toHours(elapsed) % 24;
        long minutes  = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60;
        long seconds  = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;
        long months   = days / 30;
        long years    = days / 365;

        return String.format(
            "🗓️ Since: 25 May 2023\n\n" +
            "📅 %d days\n" +
            "📆 %d months\n" +
            "🗓️ %d years\n\n" +
            "⏱️ %02d hours : %02d minutes : %02d seconds\n\n" +
            "Every second with you is special ❤️",
            days, months, years, hours, minutes, seconds
        );
    }
}
