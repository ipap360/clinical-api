package com.team360.hms.admissions.web;

import com.team360.hms.admissions.common.values.HashedString;
import com.team360.hms.admissions.common.values.RandomToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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

    public String getHash() {
        return HashedString.of(value).getValue();
    }

    public Instant getExpiry() {
        return createdAt.plusSeconds(WebServerManager.get().getRefreshTokenTimeout());
    }

}
