package com.team360.hms.admissions.web.mappers;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {

        exception.printStackTrace();

        JSONObject o = new JSONObject();
        o.put("name", exception.getClass().getCanonicalName());
        o.put("message", exception.getMessage());

        log.error(exception.getLocalizedMessage(), exception);

        // unhandled exceptions are treated as internal errors...
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(o.toString()).build();
    }
}
