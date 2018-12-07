package com.team360.hms.admissions.units.sessions;

import com.team360.hms.admissions.common.exceptions.AuthenticationException;
import com.team360.hms.admissions.common.values.HashedString;
import com.team360.hms.admissions.common.values.RandomToken;
import com.team360.hms.admissions.units.WebUtl;
import com.team360.hms.admissions.units.users.User;
import com.team360.hms.admissions.units.users.UserDao;
import com.team360.hms.admissions.web.AccessToken;
import com.team360.hms.admissions.web.RefreshToken;
import com.team360.hms.admissions.web.WebConfig;
import com.team360.hms.admissions.web.WebCookie;
import com.team360.hms.admissions.web.filters.IFilter;
import com.team360.hms.admissions.web.filters.Secured;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Path("sessions")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class SessionEndpoint {

    private static final String INVALID_CREDENTIALS = "Invalid username or password";
    private static final String SESSION_EXPIRED = "This session has expired";
    private static final Date ZERO_DATE = new Date(0);

    @Context
    ContainerRequestContext crc;

    @GET
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{uuid}")
    public Response get(@PathParam("uuid") String uuid) {
        return Response.ok().entity(new SessionDetails().load((new SessionDao()).findByUuidAndUserId(uuid, WebUtl.getUser(crc)))).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(
            @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
            @HeaderParam(IFilter.IP_HEADER) String ip,
            LoginForm form) {

        Optional<Integer> id = (new UserDao()).findByUsername(form.getUsername());
        if (!id.isPresent()) {
            throw new AuthenticationException(AuthenticationException.FAILED, INVALID_CREDENTIALS);
        }

        User u = new User();
        WebUtl.db(crc).read(u.setId(id.get()));
        HashedString pass = HashedString.fromHash(u.getPassword());
        if (!pass.isHashOf(form.getPassword())) {
            throw new AuthenticationException(AuthenticationException.FAILED, INVALID_CREDENTIALS);
        }

        Session s = new Session();

        s.setUserId(u.getId());
        s.setIp(ip);
        s.setUserAgent(userAgent);
        s.setUuid(UUID.randomUUID().toString());

        return wrapResponse(createSession(s));
    }

    @POST
    @Path("/{uuid}/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response refresh(
            @PathParam("uuid") String uuid,
            @Context HttpHeaders headers) {

        WebConfig conf = WebUtl.conf();

        Cookie cookie1 = headers.getCookies().get(conf.getRefreshTokenServerCookie());
        Cookie cookie2 = headers.getCookies().get(conf.getRefreshTokenClientCookie());

        boolean isCookieBased = false;

        String refreshToken = "";

        // if refresh token is stored in cookies, then return in cookies as well (don't expose)
        if (cookie1 != null && cookie2 != null) {
            refreshToken = cookie1.getValue() + cookie2.getValue();
            isCookieBased = true;
        }

        Optional<Integer> id = (new SessionDao()).findByUuid(uuid);
        if (!id.isPresent()) {
            throw new AuthenticationException(AuthenticationException.FAILED, SESSION_EXPIRED);
        }

        Session session = new Session();
        WebUtl.db(crc).read(session.setId(id.get()));
        HashedString secret = HashedString.fromHash(session.getSecret());
        if (!secret.isHashOf(refreshToken)) {
            throw new AuthenticationException(AuthenticationException.FAILED, SESSION_EXPIRED);
        }

        if (isCookieBased == true) {
            return wrapResponse(createSession(session));
        }

        return Response.ok().entity(createSession(session)).build();
    }

    private SessionAcquiredSuccessfully createSession(Session session) {
        RefreshToken refreshToken = RefreshToken.generate();

        session.setSecret(refreshToken.getHash());
        session.setExpiresAt(refreshToken.getExpiry());

        WebUtl.db(crc).upsert(session);

        String accessToken = AccessToken
                .withClaims(session.getUserId())
                .getValue();

        SessionAcquiredSuccessfully ok = new SessionAcquiredSuccessfully();

        ok.setAccessToken(accessToken);
        ok.setRefreshToken(refreshToken.getValue());
        ok.setDetails(new SessionDetails().load((new SessionDao()).findByUuidAndUserId(session.getUuid(), session.getUserId())));

        return ok;

    }

    @POST
    @Path("/{uuid}/expire")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(@PathParam("uuid") String uuid, @Context HttpHeaders headers) {

        Optional<Integer> id = (new SessionDao()).findByUuid(uuid);
        if (id.isPresent()) {
            Session s = new Session();
            s.setId(id.get());
            WebUtl.db(crc).read(s);
            s.setExpired();
            WebUtl.db(crc).update(s);
        }

        Map<String, Cookie> cookies = headers.getCookies();

        WebConfig conf = WebUtl.conf();

        NewCookie rc1 = setExpired(cookies.get(conf.getRefreshTokenServerCookie()));
        NewCookie rc2 = setExpired(cookies.get(conf.getRefreshTokenClientCookie()));
        NewCookie ac = setExpired(cookies.get(conf.getAccessTokenCookie()));
        NewCookie xc = setExpired(cookies.get(conf.getXsrfCookie()));

        return Response.ok().cookie(rc1, rc2, ac, xc).build();
    }

    private NewCookie setExpired(Cookie c) {
        return (c == null) ? null : new NewCookie(c, null, 0, ZERO_DATE, false, false);
    }

    private Response wrapResponse(SessionAcquiredSuccessfully entity) {

        String token = entity.getRefreshToken();
        final int pos = token.length() / 2;
        String[] parts = {token.substring(0, pos), token.substring(pos)};

        WebConfig conf = WebUtl.conf();

        int expiry = conf.getRefreshTokenTimeout();
        String path = conf.getContext() + "/sessions";

        WebCookie rc1 = WebCookie.builder()
                .name(conf.getRefreshTokenServerCookie())
                .path(path)
                .expiry(expiry)
                .body(parts[0])
                .build();

        WebCookie rc2 = WebCookie.builder()
                .name(conf.getRefreshTokenClientCookie())
                .path(path)
                .expiry(expiry)
                .httpOnly(false)
                .body(parts[1])
                .build();

        WebCookie ac = WebCookie.builder()
                .name(conf.getAccessTokenCookie())
                .body(entity.getAccessToken())
                .expiry(expiry)
                .build();

        WebCookie xc = WebCookie.builder()
                .name(conf.getXsrfCookie())
                .body(RandomToken.withLength(20).getValue())
                .expiry(expiry)
                .httpOnly(false)
                .build();

        return Response.ok().cookie(rc1.getValue(), rc2.getValue(), ac.getValue(), xc.getValue()).entity(entity.getDetails()).build();
    }

}
