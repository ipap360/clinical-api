package com.team360.hms.units.calendarEvents;

import common.exceptions.MultiValueRequestException;
import com.team360.hms.db.DBManager;
import lombok.extern.slf4j.Slf4j;
import com.team360.hms.web.filters.Secured;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.HashMap;

@Secured
@Slf4j
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("calendar-events")
public class CalendarEventsEndpoint {

    @GET
    public Response get(
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to,
            @QueryParam("patient") Integer patient) {
        CalendarEventsQuery qry = new CalendarEventsQuery(from, to, patient);
        return Response.ok().entity(DBManager.utils().execute(qry)).build();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        CalendarEvent event = new CalendarEvent();
        DBManager.utils().read(event.setId(id));
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
        DBManager.utils().upsert(event);
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
        DBManager.utils().delete(event);
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
        DBManager.utils().read(event1);

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

        DBManager.utils().upsert(event1, event2);
        return event2;
    }

}
