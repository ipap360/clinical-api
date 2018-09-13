package com.team360.hms.units.sessions;

import lombok.Data;

@Data
public class RefreshSessionRequest {

    private String uuid;

    private String refreshToken;

    private String userAgent;

    private String ip;

}
