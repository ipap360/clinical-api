package com.timelyworks.clinical.common.values;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

@ToString
@Slf4j
@EqualsAndHashCode
public class Week {

    private final ZoneId tz;
    private final Locale locale;

    private final DayOfWeek firstDayOfWeek;
    private final DayOfWeek lastDayOfWeek;

    public Week(final Locale locale) {
        this(locale, null);
    }

    public Week(final Locale locale, final String tz) {
        this.locale = locale;
        this.tz = (tz != null) ? ZoneId.of(tz) : ZoneId.of("UTC");
        this.firstDayOfWeek = WeekFields.of(this.locale).getFirstDayOfWeek();
        this.lastDayOfWeek = DayOfWeek.of(((this.firstDayOfWeek.getValue() + 5) % DayOfWeek.values().length) + 1);
    }

    private LocalDate getDate(LocalDate base) {
        return (base != null) ? base : LocalDate.now(this.tz);
    }

    public LocalDate getFirstDay(LocalDate base) {
        return getDate(base).with(TemporalAdjusters.previousOrSame(this.firstDayOfWeek));
    }

    public LocalDate getLastDay(LocalDate base) {
        return getDate(base).with(TemporalAdjusters.nextOrSame(this.lastDayOfWeek));
    }

}
