package com.team360.hms.admissions.units;

import com.team360.hms.admissions.db.DBUtils;
import com.team360.hms.admissions.web.WebConfig;
import com.team360.hms.admissions.web.WebServerManager;
import com.team360.hms.admissions.web.WebUser;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class WebUtl {

    public static DBUtils db(SecurityContext sc) {
        return new DBUtils(getUser(sc).getId());
    }

    public static DBUtils db(ContainerRequestContext crc) {
        return db(crc.getSecurityContext());
    }

    public static WebUser getUser(SecurityContext crc) {
        Principal p = crc.getUserPrincipal();
        // tbd: get locale and timezone from request
        return (p != null) ? (WebUser) p : new WebUser(0, "anonymous", null, null);
    }

    public static WebConfig conf() {
        return WebServerManager.get();
    }

}
