package com.team360.hms.units.patients;

import common.exceptions.MultiValueRequestException;
import com.team360.hms.db.DBManager;
import lombok.extern.slf4j.Slf4j;
import com.team360.hms.web.filters.Secured;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;

@Slf4j
@Path("patients")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Secured
public class PatientsEndpoint {

    @GET
    public Response get() {
        PatientsQuery qry = new PatientsQuery();
        return Response.ok().entity(DBManager.utils().execute(qry)).build();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        Patient patient = new Patient();
        DBManager.utils().read(patient.setId(id));
        PatientForm form = new PatientForm();
        return Response.ok().entity(form.load(patient)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(@PathParam("id") Integer id, PatientForm form) {
        validate(id, form);
        Patient patient = new Patient();
        patient.setId(id);
        patient.setNotes(form.getNotes());
        patient.setBirthYear(form.getBirthYear());
        patient.setGender(form.getGender());
        DBManager.utils().upsert(patient);
        return Response.ok().build();
    }

    private void validate(Integer id, PatientForm form) {
        HashMap errors = new HashMap();
        if (form.getName() == null) {
            errors.put("name", "Please fill the name");
        }
//        if (form.getGender() == null) {
//            errors.put("gender", "Please select a gender");
//        }
        if (!errors.isEmpty()) {
            throw new MultiValueRequestException(errors);
        }
    }

    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        Patient patient = new Patient();
        patient.setId(id);
        DBManager.utils().delete(patient);
        return Response.ok().build();
    }

}
