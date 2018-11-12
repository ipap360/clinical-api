package com.team360.hms.admissions.units.users;

import com.team360.hms.admissions.common.exceptions.AuthenticationException;
import com.team360.hms.admissions.common.exceptions.DomainException;
import com.team360.hms.admissions.common.exceptions.FormValidationException;
import com.team360.hms.admissions.common.values.HashedString;
import com.team360.hms.admissions.common.values.Message;
import com.team360.hms.admissions.units.WebUtl;
import com.team360.hms.admissions.units.patients.Gender;
import com.team360.hms.admissions.units.registration.Registration;
import com.team360.hms.admissions.units.registration.RegistrationConfirmForm;
import com.team360.hms.admissions.units.registration.RegistrationDao;
import com.team360.hms.admissions.units.roomAvailability.RoomAvailability;
import com.team360.hms.admissions.units.roomAvailability.RoomAvailabilityDao;
import com.team360.hms.admissions.web.WebConfig;
import com.team360.hms.admissions.web.filters.Secured;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Log4j2
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("users")
public class UsersEndpoint {

    @Context
    ContainerRequestContext crc;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserForm form) {

        String key = crc.getHeaderString("ADMIN");
        String masterKey = WebUtl.conf().getAdmin();
        if (masterKey != null && !masterKey.equals(key)) {
            throw new DomainException("You do not have adequate permissions to perform this action");
        }

        form.validate();

        Optional<Integer> id = UserDao.findByUsername(form.getUsername());
        if (id.isPresent()) {
            throw new DomainException("This username already exists!");
        }

        User user = new User().load(form);
        user.setPassword(HashedString.of(form.getPassword()).getValue());
        user.setUuid(UUID.randomUUID().toString());

        WebUtl.db(crc).upsert(user);

        return Response.status(Response.Status.CREATED).build();

    }

}
