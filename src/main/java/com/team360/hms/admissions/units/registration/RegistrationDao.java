package com.team360.hms.admissions.units.registration;

import com.team360.hms.admissions.db.DB;

import java.util.Optional;

public class RegistrationDao {

    public Optional<Integer> findByEmail(String email) {
        final String sql = "SELECT ID FROM REGISTRATIONS WHERE EMAIL = :EMAIL";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("EMAIL", email)
                .mapTo(Integer.class)
                .findFirst());
    }

}
