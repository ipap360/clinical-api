package com.team360.hms.admissions.units.calendarEvents;

import com.team360.hms.admissions.common.exceptions.MultiValueRequestException;
import com.team360.hms.admissions.web.GenericEndpoint;
import com.team360.hms.admissions.web.filters.Secured;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Secured
@Slf4j
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("calendar-events")
public class CalendarEventsEndpoint extends GenericEndpoint {

    @GET
    public Response get(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("patient") Integer patient) {

        if (from != null && to != null) {
            return Response.ok().entity((new CalendarEventDao().listByDate(from, to))).build();
        } else if (patient != null) {
            return Response.ok().entity((new CalendarEventDao().listByPatient(patient))).build();
        } else {
            return Response.ok().build();
        }
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        CalendarEvent event = new CalendarEvent();
        db().read(event.setId(id));
        CalendarEventForm form = new CalendarEventForm();
        return Response.ok().entity(form.load(event)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(@PathParam("id") Integer id, CalendarEventForm form) {
        validate(id, form);
        CalendarEvent event = new CalendarEvent();
        event.setId(id);
        event.setNotes(form.getNotes());
        event.setAdmissionDate(form.getDate());
        event.setReleaseDate(form.getDate().plusDays(form.getDuration()));
        db().upsert(event);
        return Response.ok().build();
    }

    private void validate(Integer id, CalendarEventForm form) {
        HashMap errors = new HashMap();
        if (form.getPatientId() == null) {
            errors.put("patientId", "Please select a patient");
        }
        if (form.getDate() == null) {
            errors.put("date", "Please select a valid date");
        }
        if (!errors.isEmpty()) {
            throw new MultiValueRequestException(errors);
        }
    }

    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        CalendarEvent event = new CalendarEvent();
        event.setId(id);
        db().delete(event);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/postpone")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postpone(@PathParam("id") Integer id, CalendarEventCopyForm form) {
        return Response.ok().entity(copyOrPostpone("POSTPONE", id, form)).build();
    }

    @POST
    @Path("/{id}/copy")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response copy(@PathParam("id") Integer id, CalendarEventCopyForm form) {
        return Response.ok().entity(copyOrPostpone("COPY", id, form)).build();
    }

    private CalendarEvent copyOrPostpone(String mode, Integer id, CalendarEventCopyForm form) {

        CalendarEvent event1 = new CalendarEvent();
        event1.setId(id);
        db().read(event1);

        CalendarEvent event2 = new CalendarEvent();
        event2.setNotes(form.getNotes());
        event2.setAdmissionDate(form.getDate());
        event2.setReleaseDate(form.getDate().plusDays(event1.getDuration()));

        if ("postpone".equalsIgnoreCase(mode)) {
            event1.setIsPostponed(true);
            event2.setPostponeId(event1.getId());
        } else {
            event1.setIsCopied(true);
            event1.setIsCompleted(true);
        }

        db().upsert(event1, event2);
        return event2;
    }

}
