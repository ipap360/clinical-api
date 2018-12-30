package com.timelyworks.clinical.web;

import com.google.common.hash.Hashing;
import com.timelyworks.clinical.common.values.RandomToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@ToString
@EqualsAndHashCode
public class RefreshToken {

    @Getter
    private String value;

    private Instant createdAt;

    private RefreshToken() {
        this.value = RandomToken.withLength(WebServerManager.get().getRefreshTokenLength()).getValue();
        this.createdAt = Instant.now();
    }

    public static RefreshToken generate() {
        return new RefreshToken();
    }

    public String getHash256() {
        return Hashing.sha256()
                .hashString(value, StandardCharsets.UTF_8)
                .toString();
    }

    public Instant getExpiry() {
        return createdAt.plusSeconds(WebServerManager.get().getRefreshTokenTimeout());
    }

}
