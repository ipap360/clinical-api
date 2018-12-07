package com.team360.hms.admissions.units.users;

import com.team360.hms.admissions.common.exceptions.DomainException;
import com.team360.hms.admissions.common.values.HashedString;
import com.team360.hms.admissions.units.WebUtl;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

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

        Optional<Integer> id = (new UserDao()).findByUsername(form.getUsername());
        if (id.isPresent()) {
            throw new DomainException("This username already exists!");
        }

        User user = new User().load(form);
        user.setUuid(UUID.randomUUID().toString());

        WebUtl.db(crc).upsert(user);

        return Response.status(Response.Status.CREATED).build();

    }

}
