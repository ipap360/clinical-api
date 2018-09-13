package com.team360.hms.units.sessions;

import lombok.Data;

import java.util.Map;

@Data
public class SessionAcquiredSuccessfully {

    private String accessToken;

    private String refreshToken;

    private Map<String, Object> details;

}
