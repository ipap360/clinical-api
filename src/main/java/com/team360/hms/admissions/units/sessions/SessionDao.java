package com.team360.hms.admissions.units.sessions;

import com.team360.hms.admissions.db.DB;
import com.team360.hms.admissions.db.DBMapMapper;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class SessionDao {

    public Optional<Integer> findByUuid(String uuid) {
        final String sql = "SELECT ID FROM SESSIONS WHERE UUID = :UUID AND EXPIRES_AT > :EXPIRES_AT";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("UUID", uuid)
                .bind("EXPIRES_AT", Instant.now())
                .mapTo(Integer.class)
                .findFirst());
    }

    public Map<String, Object> findByUuidAndUserId(String uuid, int userId) {
        final String sql = "SELECT U.*, S.* FROM SESSIONS S INNER JOIN USERS U ON U.ID = S.USER_ID WHERE S.USER_ID = :USER_ID AND S.UUID = :UUID AND S.EXPIRES_AT > :EXPIRES_AT";
        return DB.get().withHandle(db -> db.createQuery(sql)
                .bind("USER_ID", userId)
                .bind("UUID", uuid)
                .bind("EXPIRES_AT", Instant.now())
                .map(new DBMapMapper())
                .findOnly());
    }
}
