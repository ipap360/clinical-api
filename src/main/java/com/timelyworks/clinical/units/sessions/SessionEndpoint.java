package com.timelyworks.clinical.units.sessions;

import com.timelyworks.clinical.common.exceptions.AuthenticationException;
import com.timelyworks.clinical.common.values.HashedString;
import com.timelyworks.clinical.common.values.RandomToken;
import com.timelyworks.clinical.units.WebUtl;
import com.timelyworks.clinical.units.users.User;
import com.timelyworks.clinical.units.users.UserDao;
import com.timelyworks.clinical.web.*;
import com.timelyworks.clinical.web.filters.IFilter;
import com.timelyworks.clinical.web.filters.Secured;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private static final int MIN_TOKEN_DAYS = 5;

    @Context
    SecurityContext sc;

    @GET
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{uuid}")
    public Response get(@PathParam("uuid") String uuid) {
        return Response.ok().entity(new SessionDetails().load((new SessionDao()).findByUuidAndUserId(uuid, WebUtl.getUser(sc).getId()))).build();
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
        WebUtl.db(sc).read(u.setId(id.get()));
        HashedString pass = HashedString.fromHash(u.getPassword());
        if (!pass.isHashOf(form.getPassword())) {
            throw new AuthenticationException(AuthenticationException.FAILED, INVALID_CREDENTIALS);
        }

        Session s = new Session();

        s.setUserId(u.getId());
        s.setIp(ip);
        s.setUserAgent(userAgent);
        s.setUuid(UUID.randomUUID().toString());

        return wrapResponse(createSession(s, u));
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
        WebUtl.db(sc).read(session.setId(id.get()));
        HashedString secret = HashedString.fromHash(session.getSecret());
        if (!secret.isHashOf(refreshToken)) {
            throw new AuthenticationException(AuthenticationException.FAILED, SESSION_EXPIRED);
        }

        User user = new User();
        WebUtl.db(sc).read(user.setId(session.getUserId()));
        if (isCookieBased == true) {
            return wrapResponse(createSession(session, user));
        }

        return Response.ok().entity(createSession(session, user)).build();
    }

    private SessionAcquiredSuccessfully createSession(Session session, User user) {

        RefreshToken refreshToken = null;
        if (session.getExpiresAt() == null || session.getExpiresAt().minus(MIN_TOKEN_DAYS, ChronoUnit.DAYS).isBefore(Instant.now())) {
            refreshToken = RefreshToken.generate();
            session.setSecret(refreshToken.getHash());
            session.setExpiresAt(refreshToken.getExpiry());
        }

        WebUtl.db(sc).upsert(session);

        String accessToken = AccessToken
                .withClaims(user.getId(), user.getUsername(), user.getLocale(), user.getTimezone())
                .getValue();

        SessionAcquiredSuccessfully ok = new SessionAcquiredSuccessfully();

        ok.setAccessToken(accessToken);
        if (refreshToken != null) {
            ok.setRefreshToken(refreshToken.getValue());
        }
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
            WebUtl.db(sc).read(s);
            s.setExpired();
            WebUtl.db(sc).update(s);
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

        WebConfig conf = WebUtl.conf();
        int expiry = conf.getRefreshTokenTimeout();

        WebCookie ac = WebCookie.builder()
                .name(conf.getAccessTokenCookie())
                .body(entity.getAccessToken())
                .expiry(expiry)
                .isSecure(sc.isSecure())
                .build();

        WebCookie xc = WebCookie.builder()
                .name(conf.getXsrfCookie())
                .body(RandomToken.withLength(20).getValue())
                .expiry(expiry)
                .httpOnly(false)
                .isSecure(sc.isSecure())
                .build();

        String token = entity.getRefreshToken();
        if (token == null) {
            return Response.ok().cookie(ac.getValue(), xc.getValue()).entity(entity.getDetails()).build();
        }

        final int pos = token.length() / 2;
        String[] parts = {token.substring(0, pos), token.substring(pos)};
        String path = conf.getContext() + "/sessions";

        WebCookie rc1 = WebCookie.builder()
                .name(conf.getRefreshTokenServerCookie())
                .path(path)
                .expiry(expiry)
                .body(parts[0])
                .isSecure(sc.isSecure())
                .build();

        WebCookie rc2 = WebCookie.builder()
                .name(conf.getRefreshTokenClientCookie())
                .path(path)
                .expiry(expiry)
                .httpOnly(false)
                .body(parts[1])
                .isSecure(sc.isSecure())
                .build();

        return Response.ok().cookie(rc1.getValue(), rc2.getValue(), ac.getValue(), xc.getValue()).entity(entity.getDetails()).build();
    }

}
