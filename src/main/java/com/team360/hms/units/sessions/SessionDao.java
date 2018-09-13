package com.team360.hms.units.sessions;

import com.team360.hms.db.DBManager;
import com.team360.hms.db.DBMapMapper;
import org.jdbi.v3.core.Jdbi;

import java.time.Instant;
import java.util.Map;

public class SessionDao {

    static Jdbi JDBI = DBManager.get();

//    public Session findByUser(Integer userId) {
//        final String sql = "SELECT * FROM SESSIONS WHERE USER_ID = :USER_ID";
//        return JDBI.withHandle(com.team360.hms.db -> com.team360.hms.db.createQuery(sql)
//                .bind("USER_ID", userId)
//                .map(toEntity())
//                .findFirst());
//    }

    public static Integer findByUuid(String uuid) {
        final String sql = "SELECT ID FROM SESSIONS WHERE UUID = :UUID AND EXPIRES_AT > :EXPIRES_AT";
        return JDBI.withHandle(db -> db.createQuery(sql)
                .bind("UUID", uuid)
                .bind("EXPIRES_AT", Instant.now())
                .mapTo(Integer.class)
                .findOnly());
    }

    public static Map<String, Object> findByUuidAndUserId(String uuid, int userId) {
        final String sql = "SELECT U.*, S.* FROM SESSIONS S INNER JOIN USERS U ON U.ID = S.USER_ID WHERE S.USER_ID = :USER_ID AND S.UUID = :UUID AND S.EXPIRES_AT > :EXPIRES_AT";
        return JDBI.withHandle(db -> db.createQuery(sql)
                .bind("USER_ID", userId)
                .bind("UUID", uuid)
                .bind("EXPIRES_AT", Instant.now())
                .map(new DBMapMapper())
//                .map((ResultSet rs, StatementContext ctx) -> SessionDetails.builder()
//                        .uuid(rs.getString("S.UUID"))
//                        .expiresAt(toInstant(rs.getTimestamp("S.EXPIRES_AT")))
//                        .language(rs.getString("U.LANGUAGE"))
//                        .locale(rs.getString("U.LOCALE"))
//                        .timezone(rs.getString("U.TIMEZONE"))
//                        .name(rs.getString("U.USERNAME"))
//                        .build())
                .findOnly());
    }

}
