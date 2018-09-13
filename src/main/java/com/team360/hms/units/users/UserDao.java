package com.team360.hms.units.users;

import com.team360.hms.db.DBManager;

import java.time.Instant;

public class UserDao {

    public static Integer findByUsername(String username) {
        final String sql = "SELECT ID FROM USERS WHERE USERNAME = :USERNAME";
        return DBManager.get().withHandle(db -> db.createQuery(sql).bind("USERNAME", username).mapTo(Integer.class).findOnly());
    }

    public static Integer findByActiveSessionUuid(String sessionUuid) {
        final String sql =
                "SELECT ID INNER JOIN SESSIONS S ON S.USER_ID = U.ID WHERE S.UUID = :UUID AND S.EXPIRES_AT > :EXPIRES_AT";
        return DBManager.get().withHandle(db -> db.createQuery(sql)
                .bind("UUID", sessionUuid)
                .bind("EXPIRES_AT", Instant.now())
                .mapTo(Integer.class)
                .findOnly());
    }

}
