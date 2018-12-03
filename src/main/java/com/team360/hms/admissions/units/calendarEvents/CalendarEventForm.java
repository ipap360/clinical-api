package com.team360.hms.admissions.units.calendarEvents;

import com.team360.hms.admissions.common.exceptions.FormValidationException;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;

@Data
public class CalendarEventForm {

    private Integer id;

    private Integer patient;

    private LocalDate date;

    private Integer duration;

    private String notes;

    private Boolean isPostponed;

    private Boolean isCopied;

    private Integer postponeId;

    private LocalDate originalDate;

    CalendarEventForm load(CalendarEvent event, CalendarEvent originalEvent) {
        setId(event.getId());
        setPatient(event.getPatientId());
        setDate(event.getAdmissionDate());
        setDuration(event.getDuration());
        setNotes(event.getNotes());
        setIsPostponed(event.getIsPostponed());
        setIsCopied(event.getIsCopied());
        if (originalEvent != null) {
            setPostponeId(originalEvent.getId());
            setOriginalDate(originalEvent.getAdmissionDate());
        }
        return this;
    }

    CalendarEventForm validate() {
        HashMap errors = new HashMap();
        if (getPatient() == null) {
            errors.put("patient", "Please select a patient");
        }
        if (getDate() == null) {
            errors.put("date", "Please select a valid date");
        }
        if (getDuration() == null) {
            errors.put("duration", "Please select the duration");
        }
        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }
        return this;
    }

}
