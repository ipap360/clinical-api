package com.timelyworks.clinical.units.thresholds;

import com.timelyworks.clinical.units.WebUtl;
import com.timelyworks.clinical.web.filters.Secured;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Secured
@Log4j2
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("thresholds")
public class ThresholdsEndpoint {

    @Context
    ContainerRequestContext crc;

    @GET
    public Response get() {
        return Response.ok().entity(new ThresholdDao().list()).build();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        Threshold threshold = new Threshold();
        WebUtl.db(crc).read(threshold.setId(id));
        ThresholdForm form = new ThresholdForm();
        return Response.ok().entity(form.load(threshold)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(@PathParam("id") Integer id, ThresholdForm form) {
        form.setId(id);
        form.validate();
        Threshold threshold = new Threshold();
        WebUtl.db(crc).upsert(threshold.load(form));
        return Response.ok().entity(form.load(threshold)).build();
    }


    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        Threshold threshold = new Threshold();
        threshold.setId(id);
        WebUtl.db(crc).delete(threshold);
        return Response.ok().build();
    }

}
