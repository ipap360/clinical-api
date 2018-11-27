package com.team360.hms.admissions.units.patients;

import com.google.common.base.CaseFormat;
import com.team360.hms.admissions.db.DB;
import com.team360.hms.admissions.db.DBMapMapper;

import java.util.List;
import java.util.Map;

public class PatientDao {

    public List<Map<String, Object>> list() {
        final String sql = "SELECT * FROM PATIENTS ORDER BY NAME ASC";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .map(new DBMapMapper(CaseFormat.LOWER_CAMEL))
                .list());
    }



}
