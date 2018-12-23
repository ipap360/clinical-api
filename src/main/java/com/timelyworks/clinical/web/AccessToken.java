package com.timelyworks.clinical.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.timelyworks.clinical.common.exceptions.AuthenticationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Slf4j
@ToString
@EqualsAndHashCode
public class AccessToken {

    private static final int TOKEN_EXPIRY = WebServerManager.get().getAccessTokenTimeout();
    private static final String TOKEN_ISSUER = "nobody";
    private static final String TOKEN_CLAIM_USER = "userId";
    private static final String TOKEN_CLAIM_NAME = "name";
    private static final String TOKEN_CLAIM_LOCALE = "locale";
    private static final String TOKEN_CLAIM_TIMEZONE = "tz";

    private static Algorithm ALGORITHM;

    static {
        try {
            ALGORITHM = Algorithm.HMAC256(WebServerManager.get().getSecret());
        } catch (IllegalArgumentException e) { // | UnsupportedEncodingException
            log.debug(e.toString(), e);
        }
    }

    @Getter
    private String value;

    @Getter
    private WebUser user;

    private AccessToken(int userId, String name, String locale, String tz) {
        this.value = JWT.create()
                .withIssuer(TOKEN_ISSUER)
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRY * 1000))
                .withIssuedAt(new Date())
                .withClaim(TOKEN_CLAIM_USER, userId)
                .withClaim(TOKEN_CLAIM_NAME, name)
                .withClaim(TOKEN_CLAIM_LOCALE, locale)
                .withClaim(TOKEN_CLAIM_TIMEZONE, tz)
                .sign(ALGORITHM);

        this.user = new WebUser(userId, name, locale, tz);
    }

    private AccessToken(String value, Long leeway) {
        try {
            if (StringUtils.isEmpty(value)) {
                throw new AuthenticationException(AuthenticationException.FAILED);
            }

            Verification verification = JWT.require(ALGORITHM);
            if (leeway != null) {
                verification.acceptExpiresAt(leeway);
            }

            DecodedJWT jwt = verification.build().verify(value);

            this.value = value;

            int id = jwt.getClaim(TOKEN_CLAIM_USER).asInt();
            String name = jwt.getClaim(TOKEN_CLAIM_NAME).asString();
            String locale = jwt.getClaim(TOKEN_CLAIM_LOCALE).asString();
            String tz = jwt.getClaim(TOKEN_CLAIM_TIMEZONE).asString();

            this.user = new WebUser(id, name, locale, tz);

        } catch (TokenExpiredException e) {
            throw new AuthenticationException(AuthenticationException.EXPIRED, e.getMessage());
        } catch (JWTVerificationException e) {
            throw new AuthenticationException(AuthenticationException.FAILED, e.getMessage());
        }
    }

    public static AccessToken withClaims(int userId, String name, String locale, String tz) {
        return new AccessToken(userId, name, locale, tz);
    }

    public static AccessToken parse(String accessToken) {
        return new AccessToken(accessToken, null);
    }

    public static AccessToken parseWithExpiry(String accessToken, long leeway) {
        return new AccessToken(accessToken, leeway);
    }

}
