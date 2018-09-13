package com.team360.hms.units.registration;

import com.team360.hms.db.DBManager;

public class RegistrationDao {

    public Integer findByEmail(String email) {
        final String sql = "SELECT * FROM REGISTRATIONS WHERE EMAIL = :EMAIL";
        return DBManager.get().withHandle(db -> db.createQuery(sql)
                .bind("EMAIL", email)
                .mapTo(Integer.class)
                .findOnly());
    }

}
