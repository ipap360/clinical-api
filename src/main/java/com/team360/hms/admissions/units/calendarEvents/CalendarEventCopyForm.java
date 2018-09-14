package com.team360.hms.admissions.units.calendarEvents;

import lombok.Value;

import java.time.LocalDate;

@Value
public class CalendarEventCopyForm {

    private LocalDate date;

    private String notes;

}
