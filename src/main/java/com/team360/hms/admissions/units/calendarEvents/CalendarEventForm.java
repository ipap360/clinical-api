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

    CalendarEventForm load(CalendarEvent event) {
        setId(event.getId());
        setPatient(event.getPatientId());
        setDate(event.getAdmissionDate());
        setDuration(event.getDuration());
        setNotes(event.getNotes());
        setIsPostponed(event.getIsPostponed());
        setIsCopied(event.getIsCopied());
        setPostponeId(event.getPostponeId());
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
        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }
        return this;
    }

}
