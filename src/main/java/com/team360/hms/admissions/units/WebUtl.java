package com.team360.hms.admissions.units;

import com.team360.hms.admissions.db.DBUtils;
import com.team360.hms.admissions.web.WebConfig;
import com.team360.hms.admissions.web.WebServerManager;

import javax.ws.rs.container.ContainerRequestContext;

public class WebUtl {

    public static DBUtils db(ContainerRequestContext crc) {
        return new DBUtils(getUser(crc));
    }

    public static Integer getUser(ContainerRequestContext crc) {
        return (crc.getProperty("userId") instanceof Integer) ? (Integer) crc.getProperty("userId") : 0;
    }

    public static WebConfig conf() {
        return WebServerManager.get();
    }

}
