package com.team360.hms.admissions.units.calendarEvents;

import com.team360.hms.admissions.common.exceptions.FormValidationException;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;

@Data
public class CalendarEventForm {

    private Integer patient;

    private LocalDate date;

    private Integer duration;

    private String notes;

    CalendarEventForm load(CalendarEvent event) {
        setPatient(event.getPatientId());
        setDate(event.getAdmissionDate());
        setDuration(event.getDuration());
        setNotes(event.getNotes());
        return this;
    }

    CalendarEventForm validate(Integer id) {
        HashMap errors = new HashMap();
        if (getPatient() == null) {
            errors.put("patient", "Please select a patient");
        }
        if (getDate() == null) {
            errors.put("date", "Please select a valid date");
        }
        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }
        return this;
    }

}
