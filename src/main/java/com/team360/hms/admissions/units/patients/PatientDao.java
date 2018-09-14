package com.team360.hms.admissions.units.patients;

import com.team360.hms.admissions.db.DB;
import com.team360.hms.admissions.db.DBMapMapper;

import java.util.List;
import java.util.Map;

public class PatientDao {

    public List<Map<String, Object>> list() {
        final String sql = "SELECT * FROM PATIENTS";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .map(new DBMapMapper())
                .list());
    }

}
