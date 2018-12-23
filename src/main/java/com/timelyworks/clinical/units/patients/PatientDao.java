package com.timelyworks.clinical.units.patients;

import com.google.common.base.CaseFormat;
import com.timelyworks.clinical.db.DB;
import com.timelyworks.clinical.db.DBMapMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PatientDao {

    public List<Map<String, Object>> list(String token) {
        final String sql = "SELECT * FROM PATIENTS WHERE NAME LIKE CONCAT('%',:NAME,'%') OR :NAME IS NULL ORDER BY NAME ASC";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("NAME", token)
                .map(new DBMapMapper(CaseFormat.LOWER_CAMEL))
                .list());
    }

    public Optional<Integer> checkCodeExists(Integer id, String code) {
        final String sql = "SELECT P.ID FROM PATIENTS P WHERE :CODE IS NOT NULL AND CODE = :CODE AND P.ID <> :ID";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("ID", id != null ? id : 0)
                .bind("CODE", code)
                .mapTo(Integer.class)
                .findFirst());
    }

}
