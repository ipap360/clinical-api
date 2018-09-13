package com.team360.hms.units.calendarEvents;

import lombok.*;

import java.time.LocalDate;

@Data
public class CalendarEventForm {

    private Integer patientId;

    private LocalDate date;

    private Integer duration;

    private String notes;

    CalendarEventForm load(CalendarEvent event) {
        setPatientId(event.getPatientId());
        setDate(event.getAdmissionDate());
        setDuration(event.getDuration());
        setNotes(event.getNotes());
        return this;
    }

}
