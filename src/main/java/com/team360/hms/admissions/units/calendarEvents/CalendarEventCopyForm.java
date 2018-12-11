package com.team360.hms.admissions.units.calendarEvents;

import com.team360.hms.admissions.common.exceptions.FormValidationException;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;

@Data
public class CalendarEventCopyForm {

    private LocalDate date;

    private String notes;

    private Boolean noWeekOverlapCheck;

    CalendarEventCopyForm validate(CalendarEvent previous) {
        HashMap errors = new HashMap();
        if (getDate() == null) {
            errors.put("date", "Please select a valid date");
        }
        if (!getDate().isAfter(previous.getAdmissionDate())) {
            errors.put("date", "Please select a date after the original appointment date");
        }
        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }
        return this;
    }

}
