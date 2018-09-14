package com.team360.hms.admissions.web;

import com.team360.hms.admissions.common.values.Credentials;
import com.team360.hms.admissions.web.filters.IFilter;
import com.team360.hms.admissions.db.DBUtils;
import lombok.Getter;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

public class GenericEndpoint {

    private DBUtils db;

    public DBUtils db() {
        return this.db;
    }

    @Getter
    private Credentials credentials = null;

    @Context
    HttpHeaders headers;

    public GenericEndpoint() {
        String user = "0";
        if (headers != null) {
            user = headers.getHeaderString(IFilter.USER_ID_HEADER);
        }
        credentials = new Credentials(user);
        db = new DBUtils(credentials);
    }

}
