package com.timelyworks.clinical.web;

import lombok.Builder;
import lombok.NonNull;

import javax.ws.rs.core.NewCookie;

@Builder
public class WebCookie {

    private static final String PATH = "/";

    // Important in order to be "invisible" from the client side
    private static final boolean HTTP_ONLY = true;

    @NonNull
    private String name;

    @NonNull
    private String body;

    @Builder.Default
    private String path = PATH;

    @Builder.Default
    private Boolean httpOnly = HTTP_ONLY;

    @Builder.Default
    private Integer expiry = NewCookie.DEFAULT_MAX_AGE;

    @Builder.Default
    private Boolean isSecure = false;

    public NewCookie getValue() {
        return new NewCookie(name,
                body,
                path,
                null,
                null,
                expiry,
                isSecure,
                httpOnly);
    }

}
