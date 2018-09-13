package com.team360.hms.units.calendarEvents;

import com.team360.hms.db.DBEntityField;
import com.team360.hms.db.DBEntityMeta;

import lombok.Data;
import com.team360.hms.db.GenericEntity;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@DBEntityMeta(name="CALENDAR_EVENTS", label="Event")
public class CalendarEvent extends GenericEntity {

    @DBEntityField(name="NOTES")
    private String notes;

    @DBEntityField(name="PATIENT_ID")
    private Integer patientId;

    @DBEntityField(name="POSTPONE_ID")
    private Integer postponeId;

    @DBEntityField(name="ADMISSION_DATE")
    private LocalDate admissionDate;

    @DBEntityField(name="RELEASE_DATE")
    private LocalDate releaseDate;

    @DBEntityField(name="IS_POSTPONED")
    private Boolean isPostponed;

    @DBEntityField(name="IS_COPIED")
    private Boolean isCopied;

    @DBEntityField(name="IS_COMPLETED")
    private Boolean isCompleted;

    public Integer getDuration() {
        return admissionDate.until(releaseDate).getDays();
    }

//    @Override
//    public CalendarEvent load(Map map) {
//        super.load(map);
//        patientId = (Integer) map.get("PATIENT_ID");
//        admissionDate = (LocalDate) map.get("ADMISSION_DATE");
//        releaseDate = (LocalDate) map.get("RELEASE_DATE");
//        notes = (String) map.get("NOTES");
//        postponeId = (Integer) map.get("POSTPONE_ID");
//        return this;
//    }
//
//    @Override
//    public Map<String, ?> toMap() {
//        Map map = super.toMap();
//        map.put("PATIENT_ID", patientId);
//        map.put("ADMISSION_DATE", admissionDate);
//        map.put("RELEASE_DATE", releaseDate);
//        map.put("NOTES", notes);
//        map.put("POSTPONE_ID", postponeId);
//        return map;
//    }

}
