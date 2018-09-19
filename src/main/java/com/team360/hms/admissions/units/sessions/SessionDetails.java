package com.team360.hms.admissions.units.sessions;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class SessionDetails {

    private String uuid;

    private String name;

    private String language;

    private String locale;

    private String timezone;

    private Instant expiresAt;

    public SessionDetails load(Map<String, Object> map) {
        uuid = (String) map.get("UUID");
        name = (String) map.get("USERNAME");
        language = (String) map.get("LANGUAGE");
        locale = (String) map.get("LOCALE");
        timezone = (String) map.get("TIMEZONE");
        expiresAt = (Instant) map.get("EXPIRES_AT");
        return this;
    }

}
