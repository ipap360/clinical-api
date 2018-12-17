package com.team360.hms.admissions.units.calendarEvents;

import com.team360.hms.admissions.common.exceptions.FormValidationException;
import com.team360.hms.admissions.common.values.Warning;
import com.team360.hms.admissions.common.values.Week;
import com.team360.hms.admissions.units.WebUtl;
import com.team360.hms.admissions.web.filters.Secured;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Optional;

@Secured
@Log4j2
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("calendar-events")
public class CalendarEventsEndpoint {

    @Context
    SecurityContext sc;

    @GET
    public Response get(
            @QueryParam("from") String from,
            @QueryParam("to") String to) {

        if (from != null && to != null) {
            return Response.ok().entity((new CalendarEventDao().listByDate(from, to))).build();
        } else {
            return Response.ok().build();
        }
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        CalendarEvent event = new CalendarEvent();
        WebUtl.db(sc).read(event.setId(id));
        CalendarEventForm form = new CalendarEventForm();
        return Response.ok().entity(form.load(sc, event)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(@PathParam("id") Integer id, CalendarEventForm form) {
        form.setId(id);
        form.validate(sc);
        CalendarEvent event = new CalendarEvent();
        if (id > 0) {
            WebUtl.db(sc).read(event.setId(id));
        }
        LocalDate lastAdmissionDate = event.getAdmissionDate();
        event.load(form);
        if (BooleanUtils.isNotTrue(form.getNoWeekOverlapCheck()) && (lastAdmissionDate == null || !form.getDate().isEqual(lastAdmissionDate))) {
            checkOverlap(sc, event);
        }
        WebUtl.db(sc).upsert(event);
        return Response.ok().entity(form.load(sc, event)).build();
    }

    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        CalendarEvent event = new CalendarEvent();
        event.setId(id);
        WebUtl.db(sc).delete(event);
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

    public static void checkOverlap(SecurityContext sc, CalendarEvent event) {
        LocalDate d1 = event.getAdmissionDate();
        Week week = new Week(WebUtl.getUser(sc).getLocale());
        Optional<Map<String, Object>> result = (new CalendarEventDao()).checkOverlap(event.getId(), week.getFirstDay(d1).toString(), week.getLastDay(d1).toString(), event.getPatientId());
        if (result.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(WebUtl.getUser(sc).getLocale());
            LocalDate d = (LocalDate) result.get().get("admissionDate");
            String date = d.format(formatter);
            String name = (String) result.get().get("name");
            String body = String.format("There is a scheduled admission at %1$s for %2$s. Are you sure you want to proceed with this action?", date, name);
            throw new FormValidationException(new Warning("Warning!", body, "noWeekOverlapCheck"));
        }
    }

    private CalendarEvent copyOrPostpone(String mode, Integer id, CalendarEventCopyForm form) {
        CalendarEvent event1 = new CalendarEvent();
        event1.setId(id);
        WebUtl.db(sc).read(event1);
        form.validate(event1);

        CalendarEvent event2 = new CalendarEvent();
        event2.setPatientId(event1.getPatientId());
        event2.setAdmissionDate(form.getDate());
        if (BooleanUtils.isNotTrue(form.getNoWeekOverlapCheck())) {
            checkOverlap(sc, event2);
        }
        event2.setReleaseDate(form.getDate().plusDays(event1.getDuration()));
        event2.setNotes(form.getNotes());

        if ("postpone".equalsIgnoreCase(mode)) {
            event1.setIsPostponed(true);
            event2.setPostponeId(event1.getId());
        } else {
            event1.setIsCopied(true);
            event1.setIsCompleted(true);
        }

        WebUtl.db(sc).upsert(event1, event2);
        return event2;
    }

}
