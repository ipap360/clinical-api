package com.team360.hms.admissions.units.patients;

import com.team360.hms.admissions.db.DBEntity;
import com.team360.hms.admissions.web.GenericEndpoint;
import com.team360.hms.admissions.web.filters.Secured;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Path("patients")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Secured
public class PatientsEndpoint extends GenericEndpoint {

    @GET
    public Response get() {
//        Stream<DBEntity> s = .stream().map((map) -> new Patient().load(map));
        return Response.ok().entity(new PatientDao().list()).build();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        Patient patient = new Patient();
        db().read(patient.setId(id));
        PatientForm form = new PatientForm();
        return Response.ok().entity(form.load(patient)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(@PathParam("id") Integer id, PatientForm form) {
        form.validate(id);
        Patient patient = new Patient();
        patient.setId(id);
        db().upsert(patient.load(form));
        return Response.ok().build();
    }



    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        Patient patient = new Patient();
        patient.setId(id);
        db().delete(patient);
        return Response.ok().build();
    }

}
