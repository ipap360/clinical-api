package com.team360.hms.units.sessions;

import lombok.Data;

@Data
public class LoginForm {

    private String username;

    private String password;

    private String userAgent;

    private String ip;

}
