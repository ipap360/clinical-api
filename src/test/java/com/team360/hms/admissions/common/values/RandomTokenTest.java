package com.team360.hms.admissions.common.values;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomTokenTest {

    @Test
    public void withLength() {
        for (int i = 0; i < 1000; i++) {
            String token = RandomToken.withLength(i).getValue();
            assertTrue(token.length() == i);
        }
    }

    @Test
    public void ensureURLCompatible() throws UnsupportedEncodingException {
        for (int i = 0; i < 1000; i++) {
            String token = RandomToken.withLength(200).getValue();
            assertEquals(token, URLEncoder.encode(token, StandardCharsets.UTF_8.name()));
        }
    }
}