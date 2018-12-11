package com.team360.hms.admissions.web;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class TokenBasedSecurityContext implements SecurityContext {

    private AccessToken token;
    private final boolean secure;

    public TokenBasedSecurityContext(AccessToken token, boolean secure) {
        this.token = token;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return token.getUser();
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }

}