package com.team360.hms.admissions.units.roomAvailability;

import com.team360.hms.admissions.common.utils.DateUtils;
import com.team360.hms.admissions.db.DB;
import com.team360.hms.admissions.db.DBMapMapper;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class RoomAvailabilityDao {

    public List<Map<String, Object>> list(LocalDate d1, LocalDate d2) {

        final String sql = "SELECT D.ID AS ID, P.GENDER AS GENDER, COALESCE(COUNT(V.ID), 0) AS CNT FROM DATES D INNER JOIN CALENDAR_EVENTS V ON D.ID >= V.ADMISSION_DATE AND D.ID < V.RELEASE_DATE INNER JOIN PATIENTS P ON P.ID = V.PATIENT_ID WHERE D.ID BETWEEN :FROM_DATE AND :TO_DATE GROUP BY D.ID, P.GENDER";

        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("FROM_DATE", DateUtils.toSqlDate(d1))
                .bind("TO_DATE", DateUtils.toSqlDate(d2))
                .map(new DBMapMapper())
                .list());

    }

}
