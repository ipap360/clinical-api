package com.team360.hms.admissions.units.sessions;

import com.team360.hms.admissions.common.exceptions.AuthenticationException;
import com.team360.hms.admissions.common.values.HashedString;
import com.team360.hms.admissions.common.values.RandomToken;
import com.team360.hms.admissions.web.*;
import com.team360.hms.admissions.web.filters.IFilter;
import com.team360.hms.admissions.web.filters.Secured;
import com.team360.hms.admissions.units.users.User;
import com.team360.hms.admissions.units.users.UserDao;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Path("sessions")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes(MediaType.APPLICATION_JSON)
public class SessionEndpoint extends GenericEndpoint {

    private static final String INVALID_CREDENTIALS = "Invalid username or password";
    private static final String SESSION_EXPIRED = "This session has expired";
    private static final Date ZERO_DATE = new Date(0);

    WebServerConfig config;

    public SessionEndpoint() {
        super();
        config = WebServerManager.get();
    }

    @GET
    @Secured
    public Response get(@HeaderParam("userId") Integer userId, @QueryParam("uuid") String uuid) {
        return Response.ok().entity(new SessionDetails().load(SessionDao.findByUuidAndUserId(uuid, userId))).build();
    }

    @POST
    public Response login(
            @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
            @HeaderParam(IFilter.IP_HEADER) String ip,
            LoginForm form) {

        Optional<Integer> id = UserDao.findByUsername(form.getUsername());
        if (!id.isPresent()) {
            throw new AuthenticationException(AuthenticationException.FAILED, INVALID_CREDENTIALS);
        }

        User u = new User();
        db().read(u.setId(id.get()));
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
    @Path("/refresh")
    public Response refresh(
            RefreshSessionRequest request,
            @Context HttpHeaders headers) {

        Cookie cookie1 = headers.getCookies().get(config.getRefreshTokenServerCookie());
        Cookie cookie2 = headers.getCookies().get(config.getRefreshTokenClientCookie());

        boolean isCookieBased = false;

        // if refresh token is stored in cookies, then return in cookies as well (don't expose)
        if (cookie1 != null && cookie2 != null) {
            request.setRefreshToken(cookie1.getValue() + cookie2.getValue());
            isCookieBased = true;
        }

        Optional<Integer> id = SessionDao.findByUuid(request.getUuid());
        if (!id.isPresent()) {
            throw new AuthenticationException(AuthenticationException.FAILED, SESSION_EXPIRED);
        }

        Session session = new Session();
        db().read(session.setId(id.get()));
        HashedString secret = HashedString.fromHash(session.getSecret());
        if (!secret.isHashOf(request.getRefreshToken())) {
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

        db().upsert(session);

        String accessToken = AccessToken
                .withClaims(session.getUserId())
                .getValue();

        SessionAcquiredSuccessfully ok = new SessionAcquiredSuccessfully();

        ok.setAccessToken(accessToken);
        ok.setRefreshToken(refreshToken.getValue());
        ok.setDetails(new SessionDetails().load(SessionDao.findByUuidAndUserId(session.getUuid(), session.getUserId())));

        return ok;

    }

    @POST
    @Path("/expire")
    public Response logout(ExpireSessionRequest request, @Context HttpHeaders headers) {

        Optional<Integer> id = SessionDao.findByUuid(request.getUuid());
        if (id.isPresent()) {
            Session s = new Session();
            s.setId(id.get());
            db().read(s);
            s.setExpired();
            db().update(s);
        }

        Map<String, Cookie> cookies = headers.getCookies();

        NewCookie rc1 = setExpired(cookies.get(config.getRefreshTokenServerCookie()));
        NewCookie rc2 = setExpired(cookies.get(config.getRefreshTokenClientCookie()));
        NewCookie ac = setExpired(cookies.get(config.getAccessTokenCookie()));
        NewCookie xc = setExpired(cookies.get(config.getXsrfCookie()));

        return Response.ok().cookie(rc1, rc2, ac, xc).build();
    }

    private NewCookie setExpired(Cookie c) {
        return (c == null) ? null : new NewCookie(c, null, 0, ZERO_DATE, false, false);
    }

    private Response wrapResponse(SessionAcquiredSuccessfully entity) {

        String token = entity.getRefreshToken();
        final int pos = token.length() / 2;
        String[] parts = {token.substring(0, pos), token.substring(pos)};

        int expiry = config.getRefreshTokenTimeout();
        String path = config.getContext() + "/sessions";

        WebCookie rc1 = WebCookie.builder()
                .name(config.getRefreshTokenServerCookie())
                .path(path)
                .expiry(expiry)
                .body(parts[0])
                .build();

        WebCookie rc2 = WebCookie.builder()
                .name(config.getRefreshTokenClientCookie())
                .path(path)
                .expiry(expiry)
                .httpOnly(false)
                .body(parts[1])
                .build();

        WebCookie ac = WebCookie.builder()
                .name(config.getAccessTokenCookie())
                .body(entity.getAccessToken())
                .expiry(expiry)
                .build();

        WebCookie xc = WebCookie.builder()
                .name(config.getXsrfCookie())
                .body(RandomToken.withLength(20).getValue())
                .expiry(expiry)
                .httpOnly(false)
                .build();

        return Response.ok().cookie(rc1.getValue(), rc2.getValue(), ac.getValue(), xc.getValue()).entity(entity.getDetails()).build();
    }

}
