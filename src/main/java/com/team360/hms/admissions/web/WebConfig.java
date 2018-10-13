package com.team360.hms.admissions.web;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;
import java.net.URISyntaxException;

@Value
@Builder
public class WebConfig {

    @Builder.Default
    private boolean isSecure = false;

    @NonNull
    private String uri;

    @NonNull
    private String context;

    @NonNull
    private String secret;

    @Builder.Default
    private String domainName = "localhost";

    @Builder.Default
    private String xsrfCookie = "XSRF-TOKEN";

    @Builder.Default
    private String xsrfHeader = "X-XSRF-TOKEN";

    @Builder.Default
    private String accessTokenCookie = "aou8";

    @Builder.Default
    private String refreshTokenClientCookie = "cli3ntRT";

    @Builder.Default
    private String refreshTokenServerCookie = "keep4live";

    @Builder.Default
    private Integer accessTokenTimeout = 20 * 60; // 20 minutes

    @Builder.Default
    private Integer refreshTokenTimeout = 7 * 24 * 60 * 60; // 7 days

    @Builder.Default
    private Integer refreshTokenLength = 80;

}
