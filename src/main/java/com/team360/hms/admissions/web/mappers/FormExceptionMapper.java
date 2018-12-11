package com.team360.hms.admissions.web.mappers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.team360.hms.admissions.common.exceptions.FormValidationException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class FormExceptionMapper implements ExceptionMapper<FormValidationException> {

    private static Gson gson = new GsonBuilder().create();

    @Override
    public Response toResponse(FormValidationException exception) {

        JSONObject o = new JSONObject();

        o.put("name", exception.getClass().getSimpleName());
        o.put("message", exception.getMessage());

        if (exception.getErrors() != null) {
            JSONObject errors = new JSONObject();
            exception.getErrors().forEach((key, value) -> {
                errors.put(key, value);
            });
            o.put("errors", errors);
        }

        if (exception.getWarning() != null) {
            o.put("warning", new JSONObject(gson.toJson(exception.getWarning())));
        }

        // domain exceptions are treated as bad requests...
        return Response.status(Status.BAD_REQUEST).entity(o.toString()).build();
    }
}
