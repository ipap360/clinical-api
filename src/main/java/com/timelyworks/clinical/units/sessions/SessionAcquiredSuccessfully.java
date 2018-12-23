package com.timelyworks.clinical.units.sessions;

import lombok.Data;

@Data
public class SessionAcquiredSuccessfully {

    private String accessToken;

    private String refreshToken;

    private SessionDetails details;

}
