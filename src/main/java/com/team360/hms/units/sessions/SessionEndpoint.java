package com.team360.hms.units.sessions;

import common.exceptions.AuthenticationException;
import common.values.HashedString;
import common.values.RandomToken;
import com.team360.hms.db.DBManager;
import lombok.extern.slf4j.Slf4j;
import com.team360.hms.units.users.User;
import com.team360.hms.units.users.UserDao;
import com.team360.hms.web.*;
import com.team360.hms.web.filters.Secured;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Date;
import java.util.Map;

@Slf4j
@Path("sessions")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes(MediaType.APPLICATION_JSON)
public class SessionEndpoint {

    private static final String INVALID_CREDENTIALS = "Invalid username or password";
    private static final String SESSION_EXPIRED = "This session has expired";
    private static final Date ZERO_DATE = new Date(0);

    @Context
    HttpHeaders headers;

    @Context
    UriInfo info;

    RequestWrapper request;

    WebServerConfig config;

    public SessionEndpoint() {
        request = RequestWrapper.of(headers, info);
        config = WebServerManager.get();
    }

    @GET
    @Secured
    public Response get(@HeaderParam("userId") Integer userId, @QueryParam("uuid") String uuid) {
        return Response.ok().entity(SessionDao.findByUuidAndUserId(uuid, userId)).build();
    }

    @POST
    public Response login(LoginForm form) {

        Integer id = UserDao.findByUsername(form.getUsername());
        if (id == null) {
            throw new AuthenticationException(AuthenticationException.FAILED, INVALID_CREDENTIALS);
        }

        User u = new User();
        DBManager.utils().read(u.setId(id));
        HashedString pass = HashedString.fromHash(u.getPassword());
        if (!pass.isHashOf(form.getPassword())) {
            throw new AuthenticationException(AuthenticationException.FAILED, INVALID_CREDENTIALS);
        }

        Session s = new Session();

        s.setUserId(u.getId());
        s.setIp(form.getIp());
        s.setUserAgent(form.getUserAgent());

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

        Integer id = SessionDao.findByUuid(request.getUuid());
        if (id == null) {
            throw new AuthenticationException(AuthenticationException.FAILED, SESSION_EXPIRED);
        }

        Session session = new Session();
        DBManager.utils().read(session.setId(id));
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

        DBManager.utils().upsert(session);

        String accessToken = AccessToken
                .withClaims(session.getUserId())
                .getValue();

        SessionAcquiredSuccessfully ok = new SessionAcquiredSuccessfully();

        ok.setAccessToken(accessToken);
        ok.setAccessToken(refreshToken.getValue());
        ok.setDetails(SessionDao.findByUuidAndUserId(session.getUuid(), session.getUserId()));

        return ok;

    }

    @POST
    @Path("/expire")
    public Response logout(ExpireSessionRequest request, @Context HttpHeaders headers) {


        Integer id = SessionDao.findByUuid(request.getUuid());
        if (id != null) {
            Session s = new Session();
            s.setId(id);
            DBManager.utils().read(s);
            s.setExpired();
            DBManager.utils().update(s);
        }

        Map<String, Cookie> cookies = headers.getCookies();

        NewCookie rc1 = setExpired(cookies.get(config.getRefreshTokenServerCookie()));
        NewCookie rc2 = setExpired(cookies.get(config.getRefreshTokenClientCookie()));
        NewCookie ac = setExpired(cookies.get(config.getAccessTokenCookie()));
        NewCookie xc = setExpired(cookies.get(config.getXsrfCookie()));

        return Response.ok().cookie(rc1, rc2, ac, xc).build();
    }

    private NewCookie setExpired(Cookie cookie) {
        if (cookie == null) {
            return null;
        }
        return new NewCookie(cookie, null, 0, ZERO_DATE, false, false);
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
