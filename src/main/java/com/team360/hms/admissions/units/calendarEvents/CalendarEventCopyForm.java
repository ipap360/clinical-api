package com.team360.hms.admissions.units.calendarEvents;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CalendarEventCopyForm {

    private LocalDate date;

    private String notes;

}
