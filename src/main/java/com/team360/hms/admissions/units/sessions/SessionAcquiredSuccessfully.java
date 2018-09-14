package com.team360.hms.admissions.units.sessions;

import lombok.Data;

@Data
public class SessionAcquiredSuccessfully {

    private String accessToken;

    private String refreshToken;

    private SessionDetails details;

}
