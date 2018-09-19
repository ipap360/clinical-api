package com.team360.hms.admissions.common.utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public class DateUtils {

    public static final String UTC = "UTC";
    public static final ZoneId UTC_ZONE = ZoneId.of(UTC);

    private static long toMilliseconds(LocalDate date) {
        return date.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli();
    }

    public static java.util.Date toJavaDate(LocalDate date) {
        return new java.util.Date(toMilliseconds(date));
    }

    public static java.sql.Date toSqlDate(LocalDate date) {
        return new java.sql.Date(toMilliseconds(date));
    }

    public static java.sql.Time toSqlTime(LocalTime time) {
        return (time != null) ? java.sql.Time.valueOf(time) : null;
    }

    //time.getHour() + ":" + time.getMinute() + time.getSecond()

    public static Instant toInstant(java.sql.Timestamp ts) {
        return (ts != null) ? ts.toInstant() : null;
    }

    public static Timestamp toTimestamp(Instant instant) {
        return (instant != null) ? Timestamp.from(instant) : null;
    }

    public static LocalDate toLocalDate(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate toLocalDate(String d) {
        return (d == null) ? null : LocalDate.parse(d);
    }

    public static LocalDate toLocalDate(java.sql.Date d) {
        return (d == null) ? null : d.toLocalDate();
    }

    public static LocalTime toLocalTime(java.sql.Time t) {
        return (t == null) ? null : t.toLocalTime();
    }

    public static LocalTime toLocalTime(String t) {
        return (t == null) ? null : LocalTime.parse(t);
    }

}
