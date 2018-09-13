package com.team360.hms.units.calendarEvents;

import com.team360.hms.db.DBQuery;
import com.team360.hms.db.DBQueryParam;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CalendarEventsQuery implements DBQuery {

    @DBQueryParam(name="FROM_DATE")
    LocalDate fromDate;

    @DBQueryParam(name="TO_DATE")
    LocalDate toDate;

    @DBQueryParam(name="PATIENT_ID")
    Integer patientId;

    @Override
    public String getSql() {
        StringBuilder statement = new StringBuilder("1 = 1");

        if (getFromDate() != null && getToDate() != null) {
            statement.append(" AND ((ADMISSION_DATE BETWEEN :FROM_DATE AND :TO_DATE) OR (RELEASE_DATE BETWEEN :FROM_DATE AND :TO_DATE))");
        } else {
            statement.append(" AND 1 = 0");
        }

        if (getPatientId() != null) {
            statement.append(" AND PATIENT_ID = :PATIENT_ID");
        }

        return "SELECT C.ID, C.NOTES AS EVENT_NOTES, C.ADMISSION_DATE, C.RELEASE_DATE, C.IS_POSTPONED, P.NAME, P.CODE, P.NOTES AS PATIENT_NOTES, P.GENDER FROM CALENDAR_EVENTS C INNER JOIN PATIENTS P ON P.ID = C.PATIENT_ID WHERE " + statement.toString() + " ORDER BY ADMISSION_DATE ASC, RELEASE_DATE ASC";
    }
}
