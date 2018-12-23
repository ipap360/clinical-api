package com.timelyworks.clinical.units.sessions;

import com.timelyworks.clinical.common.GenericEntity;
import com.timelyworks.clinical.db.DBEntityField;
import com.timelyworks.clinical.db.DBEntityMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
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

}
