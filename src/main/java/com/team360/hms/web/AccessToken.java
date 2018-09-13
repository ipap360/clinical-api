package com.team360.hms.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import common.exceptions.AuthenticationException;
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
    private static Algorithm ALGORITHM;
//    private static final String TOKEN_CLAIM_KEY = "tokenId";
//    private static final String TOKEN_CLAIM_NAME = "displayName";

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
    private int userId;

    @Getter
    private String displayName;

    private AccessToken(int userId) {

        this.userId = userId;
//        this.refreshTokenId = refreshTokenId;

        this.value = JWT.create()
                .withIssuer(TOKEN_ISSUER)
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRY * 1000))
                .withIssuedAt(new Date())
                .withClaim(TOKEN_CLAIM_USER, userId)
//                .withClaim(TOKEN_CLAIM_KEY, refreshTokenId)
//                .withClaim(TOKEN_CLAIM_NAME, displayName)
                .sign(ALGORITHM);
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
            this.userId = jwt.getClaim(TOKEN_CLAIM_USER).asInt();
//            this.refreshTokenId = jwt.getClaim(TOKEN_CLAIM_KEY).asInt();
//            this.displayName = jwt.getClaim(TOKEN_CLAIM_NAME).asString();

        } catch (TokenExpiredException e) {
            throw new AuthenticationException(AuthenticationException.EXPIRED, e.getMessage());
        } catch (JWTVerificationException e) {
            throw new AuthenticationException(AuthenticationException.FAILED, e.getMessage());
        }
    }

    public static AccessToken withClaims(int userId) {
        return new AccessToken(userId);
    }

    public static AccessToken parse(String accessToken) {
        return new AccessToken(accessToken, null);
    }

    public static AccessToken parseWithExpiry(String accessToken, long leeway) {
        return new AccessToken(accessToken, leeway);
    }

}
