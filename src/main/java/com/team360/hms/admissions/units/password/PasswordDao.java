package com.team360.hms.admissions.units.password;

import com.team360.hms.admissions.db.DB;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class PasswordDao {

    public Optional<Integer> findActiveByEmailAndStatus(String email, List<String> statuses) {
        final String sql = "SELECT ID FROM PASSWORD_RESET_REQUESTS WHERE EMAIL = :EMAIL AND EXPIRES_AT > :EXPIRES_AT AND STATUS IN (<STATUSES>)";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("EMAIL", email)
                .bind("EXPIRES_AT", Instant.now())
                .bindList("STATUSES", statuses)
                .mapTo(Integer.class)
                .findFirst());
    }

    public Integer countBySameIpStatusCreation (String ip, String status, Instant since) {
        final String sql = "SELECT COUNT(ID) FROM PASSWORD_RESET_REQUESTS WHERE IP = :IP AND STATUS = :STATUS AND CREATED_AT > :CREATED_AT";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("IP", ip)
                .bind("STATUS", status)
                .bind("CREATED_AT", since)
                .mapTo(Integer.class)
                .findOnly());
    }

}
