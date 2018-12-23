package com.timelyworks.clinical.units.profile;

import com.timelyworks.clinical.units.WebUtl;
import com.timelyworks.clinical.units.users.User;
import com.timelyworks.clinical.web.filters.Secured;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Secured
@Log4j2
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("profile")
public class ProfileEndpoint {

    @Context
    SecurityContext sc;

    @GET
    public Response view() {
        User u = getUser();
        return Response.ok().entity((new ProfileForm()).load(u)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(ProfileForm form) {
        form.validate();
        User u = getUser();
        WebUtl.db(sc).upsert(u.load(form));
        return Response.ok().entity(form.load(u)).build();
    }

    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(ChangePasswordForm form) {
        User u = getUser();
        form.validate(u);
        WebUtl.db(sc).upsert(u.load(form));
        return Response.ok().build();
    }

    private User getUser() {
        Integer userId = WebUtl.getUser(sc).getId();
        User user = new User();
        WebUtl.db(sc).read(user.setId(userId));
        return user;
    }

}
