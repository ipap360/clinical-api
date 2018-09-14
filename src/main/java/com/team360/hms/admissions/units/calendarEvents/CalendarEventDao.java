package com.team360.hms.admissions.units.calendarEvents;

import com.team360.hms.admissions.db.DB;
import com.team360.hms.admissions.db.DBMapMapper;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CalendarEventDao {

    public List<Map<String, Object>> listByDate(String from, String to) {
        final String sql = "SELECT C.ID, C.NOTES AS EVENT_NOTES, C.ADMISSION_DATE, C.RELEASE_DATE, C.IS_POSTPONED, P.NAME, P.CODE, P.NOTES AS PATIENT_NOTES, P.GENDER FROM CALENDAR_EVENTS C INNER JOIN PATIENTS P ON P.ID = C.PATIENT_ID WHERE ADMISSION_DATE <= :TO_DATE AND RELEASE_DATE > :FROM_DATE ORDER BY ADMISSION_DATE ASC, RELEASE_DATE ASC";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("FROM_DATE", from)
                .bind("TO_DATE", to)
                .map(new DBMapMapper())
                .list());
    }

    public List<Map<String, Object>> listByPatient(Integer patientId) {
        final String sql = "SELECT C.ID, C.NOTES AS EVENT_NOTES, C.ADMISSION_DATE, C.RELEASE_DATE, C.IS_POSTPONED, P.NAME, P.CODE, P.NOTES AS PATIENT_NOTES, P.GENDER FROM CALENDAR_EVENTS C INNER JOIN PATIENTS P ON P.ID = C.PATIENT_ID WHERE P.ID = :PATIENT_ID ORDER BY ADMISSION_DATE ASC, RELEASE_DATE ASC";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("PATIENT_ID", patientId)
                .map(new DBMapMapper())
                .list());
    }


}
