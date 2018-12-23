package com.timelyworks.clinical.units.users;

import com.timelyworks.clinical.db.DB;

import java.time.Instant;
import java.util.Optional;

public class UserDao {

    public Optional<Integer> findByUsername(String username) {
        final String sql = "SELECT ID FROM USERS WHERE USERNAME = :USERNAME";
        return DB.get().withHandle(db -> db.createQuery(sql).bind("USERNAME", username).mapTo(Integer.class).findFirst());
    }

    public Optional<Integer> findByActiveSessionUuid(String sessionUuid) {
        final String sql =
                "SELECT ID INNER JOIN SESSIONS S ON S.USER_ID = U.ID WHERE S.UUID = :UUID AND S.EXPIRES_AT > :EXPIRES_AT";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("UUID", sessionUuid)
                .bind("EXPIRES_AT", Instant.now())
                .mapTo(Integer.class)
                .findFirst());
    }

}
