package com.timelyworks.clinical.units.rooms;

import com.timelyworks.clinical.units.WebUtl;
import com.timelyworks.clinical.units.patients.Gender;
import com.timelyworks.clinical.web.filters.Secured;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Secured
@Log4j2
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("rooms")
public class RoomsEndpoint {

    private static List<Integer> roomPartialSums;

    @Context
    ContainerRequestContext crc;

    static void maybeCalculatePartSums(boolean force) {
        if (roomPartialSums != null && !force) {
            return;
        }

        List<Integer> rooms = new RoomDao().capacities();
        log.debug("Calculating sub sums of " + rooms.size() + " rooms...");
        roomPartialSums = listSubSums(rooms).stream().distinct().collect(Collectors.toList());
    }

    static List<Integer> listSubSums(List<Integer> list) {
        List<Integer> subSums = new ArrayList();
        int n = list.size();
        for (int i = 0; i < (1 << n); i++) {
            int sum = 0;
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    sum += list.get(j);
                }
            }
            subSums.add(sum);
        }
        return subSums;
    }

    static int[] freePerGender(List<Integer> capacities, int total, int M, int F) {

        if (total < M + F) {
            return new int[]{0, 0};
        }

        int capM = bestFit(capacities, M);
        int capF = bestFit(capacities, F);

        int freeM, freeF;

        freeM = total - capF - M;
        freeF = total - capM - F;

        if (freeM >= 0 || freeF >= 0) {
            return new int[]{freeM, freeF};
        }

        if (M < F) {
            return new int[]{capM - M + freeF, 0};
        }

        if (F < M) {
            return new int[]{0, capF - F + freeM};
        }

        return new int[]{0, 0};
    }

    static int bestFit(List<Integer> list, Integer number) {
        int pick1 = list.stream().filter(i -> i >= number).min(Comparator.comparingInt(i -> i - number)).orElse(0);
        return (pick1 > 0) ? pick1 : list.stream().filter(i -> i < number).max(Comparator.comparingInt(i -> i)).orElse(0);
    }

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
        maybeCalculatePartSums(true);
        return Response.ok().entity(form.load(room)).build();
    }

    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        Room room = new Room();
        room.setId(id);
        WebUtl.db(crc).delete(room);
        maybeCalculatePartSums(true);
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

            RoomDao roomDao = new RoomDao();
            List<Integer> rooms = roomDao.capacities();
            maybeCalculatePartSums(false);

            List<Map<String, Object>> map = roomDao.admissionsPerGenderPerDate(d1, d2);
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

            int total = rooms.stream().mapToInt(Integer::intValue).sum();
            days.forEach((k, v) -> {
                int[] free = freePerGender(roomPartialSums, total, v.getMale(), v.getFemale());
                v.setM(free[0]);
                v.setF(free[1]);
                v.setTotal(total - v.getMale() - v.getFemale());
            });

            return days;
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Please select a valid period");
        }
    }

}
