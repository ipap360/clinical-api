package com.team360.hms.admissions.units.sessions;

import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.db.DBEntityMeta;
import com.team360.hms.admissions.common.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper=true)
@DBEntityMeta(name = "SESSIONS", label = "Session")
public class Session extends GenericEntity {

    @DBEntityField(name = "UUID")
    private String uuid;

    @DBEntityField(name = "USER_ID")
    private Integer userId;

    @DBEntityField(name = "SECRET")
    private String secret;

    @DBEntityField(name = "IP")
    private String ip;

    @DBEntityField(name = "USER_AGENT")
    private String userAgent;

    @DBEntityField(name = "EXPIRES_AT")
    private Instant expiresAt;

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public Session setExpired() {
        setExpiresAt(Instant.now().minusMillis(1L));
        return this;
    }

/*    @Override
    public DBTable getTable() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }*/

/*    @Override
    public DBEntity load(Map map) {

        super.load(map);

        setUuid((String) map.get("UUID"));
        setIp((String) map.get("IP"));
        setUserAgent((String) map.get("USER_AGENT"));
        setUserId((Integer) map.get("USER_ID"));
        setSecret((String) map.get("SECRET"));
        setExpiresAt((Instant) map.get("EXPIRES_AT"));

        return this;
    }

    @Override
    public Map toMap() {

        Map m = super.toMap();

        m.put("UUID", getUuid());
        m.put("IP", getIp());
        m.put("USER_AGENT", getUserAgent());
        m.put("USER_ID", getUserId());
        m.put("SECRET", getSecret());
        m.put("EXPIRES_AT", getExpiresAt());

        return m;
    }*/
}
