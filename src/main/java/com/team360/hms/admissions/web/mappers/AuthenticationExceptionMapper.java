package com.team360.hms.admissions.web.mappers;

import com.team360.hms.admissions.common.exceptions.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    @Override
    public Response toResponse(AuthenticationException exception) {

        JSONObject o = new JSONObject();

        o.put("code", exception.getCode());
        o.put("message", exception.getMessage());

        return Response.status(Response.Status.UNAUTHORIZED).entity(o.toString()).build();
    }

}
