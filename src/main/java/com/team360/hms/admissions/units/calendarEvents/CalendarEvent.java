package com.team360.hms.admissions.units.calendarEvents;

import com.team360.hms.admissions.db.DBEntityMeta;
import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.common.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@DBEntityMeta(name = "CALENDAR_EVENTS", label = "Event")
public class CalendarEvent extends GenericEntity implements Cloneable {

    @DBEntityField(name = "NOTES")
    private String notes;

    @DBEntityField(name = "PATIENT_ID")
    private Integer patientId;

    @DBEntityField(name = "POSTPONE_ID")
    private Integer postponeId;

    @DBEntityField(name = "ADMISSION_DATE")
    private LocalDate admissionDate;

    @DBEntityField(name = "RELEASE_DATE")
    private LocalDate releaseDate;

    @DBEntityField(name = "IS_POSTPONED")
    private Boolean isPostponed;

    @DBEntityField(name = "IS_COPIED")
    private Boolean isCopied;

    @DBEntityField(name = "IS_COMPLETED")
    private Boolean isCompleted;

    public Integer getDuration() {
        return admissionDate.until(releaseDate).getDays();
    }

    public CalendarEvent load(CalendarEventForm form) {
        setId(form.getId());
        setPatientId(form.getPatient());
        setNotes(form.getNotes());
        setAdmissionDate(form.getDate());
        setReleaseDate(form.getDate().plusDays(form.getDuration()));
        return this;
    }

}
