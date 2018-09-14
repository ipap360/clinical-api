package com.team360.hms.admissions.units.roomAvailability;

import com.team360.hms.admissions.web.GenericEndpoint;
import com.team360.hms.admissions.web.filters.Secured;
import com.team360.hms.admissions.units.patients.Gender;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Secured
@Slf4j
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("room-availability")
public class RoomAvailabilityEndpoint extends GenericEndpoint {

    private static final int BEDS_PER_ROOM = 6;
    private static final int TOTAL_ROOMS = 3;
    private static final int LIMITED = 3;
    private static final int WARNING = 7;

    @GET
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

            List<Map<String, Object>> map = new RoomAvailabilityDao().list(d1, d2);
            map.forEach((v) -> {

                // todo: check what is going on with the case
                Date d = (Date) v.get("id");
                int cnt = ((Long) v.get("cnt")).intValue();
                String gender = (String) v.get("gender");

                RoomAvailability props = days.get(d.toLocalDate());

                if (Gender.MALE.name().equals(gender)) {
                    props.setMale(cnt);
                } else if (Gender.FEMALE.name().equals(gender)) {
                    props.setFemale(cnt);
                }
            });

            days.forEach((k, v) -> {

                int f = v.getFemale();
                int m = v.getMale();

                int partF = (f % BEDS_PER_ROOM != 0) ? 1 : 0;
                int partM = (m % BEDS_PER_ROOM != 0) ? 1 : 0;

                int Frooms = f / BEDS_PER_ROOM + partF;
                int Mrooms = m / BEDS_PER_ROOM + partM;

                int free = (TOTAL_ROOMS - Frooms - Mrooms) * BEDS_PER_ROOM;

                int freeF = BEDS_PER_ROOM * partF - f % BEDS_PER_ROOM + free;
                int freeM = BEDS_PER_ROOM * partM - m % BEDS_PER_ROOM + free;

                v.setF(getIndicator(freeF));
                v.setM(getIndicator(freeM));

            });

            return days;
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Please select a valid period");
        }
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
