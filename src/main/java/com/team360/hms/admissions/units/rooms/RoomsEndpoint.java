package com.team360.hms.admissions.units.rooms;

import com.team360.hms.admissions.units.WebUtl;
import com.team360.hms.admissions.units.patients.Gender;
import com.team360.hms.admissions.web.filters.Secured;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Secured
@Log4j2
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("rooms")
public class RoomsEndpoint {

    private static final int BEDS_PER_ROOM = 6;
    private static final int TOTAL_ROOMS = 3;
    private static final int LIMITED = 3;
    private static final int WARNING = 7;

    @Context
    ContainerRequestContext crc;

    @GET
    public Response get() {
        return Response.ok().entity(new RoomDao().list()).build();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        Room room = new Room();
        WebUtl.db(crc).read(room.setId(id));
        RoomForm form = new RoomForm();
        return Response.ok().entity(form.load(room)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(@PathParam("id") Integer id, RoomForm form) {
        form.setId(id);
        form.validate();
        Room room = new Room();
        WebUtl.db(crc).upsert(room.load(form));
        return Response.ok().entity(form.load(room)).build();
    }


    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        Room room = new Room();
        room.setId(id);
        WebUtl.db(crc).delete(room);
        return Response.ok().build();
    }

    @GET
    @Path("/availability")
    public Response get(
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {

        return Response.ok().entity(process(LocalDate.parse(from), LocalDate.parse(to))).build();

    }

    public Map<LocalDate, RoomAvailability> process(LocalDate d1, LocalDate d2) {
        try {
            Map<LocalDate, RoomAvailability> days = new HashMap();
            for (LocalDate d = d1; !d.isAfter(d2); d = d.plusDays(1)) {
                days.put(d, new RoomAvailability());
            }

            List<Integer> rooms = new RoomDao().capacities();

            List<Map<String, Object>> map = new RoomDao().admissionsPerGenderPerDate(d1, d2);
            map.forEach((v) -> {
                // todo: check what is going on with the case
                LocalDate d = (LocalDate) v.get("ID");
                int cnt = ((Long) v.get("CNT")).intValue();
                String gender = (String) v.get("GENDER");

                RoomAvailability props = days.get(d);

                if (Gender.MALE.name().equals(gender)) {
                    props.setMale(cnt);
                } else if (Gender.FEMALE.name().equals(gender)) {
                    props.setFemale(cnt);
                }
            });

            days.forEach((k, v) -> {
                int[] free = freePerGender(rooms, v.getMale(), v.getFemale());
                v.setF(getIndicator(free[1]));
                v.setM(getIndicator(free[0]));

            });

            return days;
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Please select a valid period");
        }
    }

    static int[] freePerGender(List<Integer> rooms, int m, int f) {
        List<Integer> dailyRooms = new ArrayList(rooms);

        int[] val = new int[]{m, f};
        int[] remain = new int[]{0, 0};

        while (val[0] > 0 || val[1] > 0) {
            int i = (val[0] >= val[1]) ? 0 : 1;
            int capacity = closest(dailyRooms, val[i]);
            if (capacity <= 0) {
                remain[0] = (val[0] > 0) ? -val[0] : remain[0];
                remain[1] = (val[1] > 0) ? -val[1] : remain[1];
                break;
            }
            val[i] = val[i] - capacity;
            if (val[i] < 0) {
                remain[i] = -val[i];
                val[i] = 0;
            }
            int p = dailyRooms.indexOf(capacity);
            dailyRooms.remove(p);
        }

        int free = dailyRooms.stream().mapToInt(Integer::intValue).sum();

        remain[0] += free;
        remain[1] += free;

        return remain;
    }

    static int closest(List<Integer> list, Integer number) {
        return list.stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - number))).orElse(0);
    }

    private String getIndicator(int free) {
        if (free <= 0) {
            return "FULL";
        } else if (free <= LIMITED) {
            return "LIMITED";
        } else if (free <= WARNING) {
            return "WARNING";
        } else {
            return "";
        }
    }

}
