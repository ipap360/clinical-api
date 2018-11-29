package com.team360.hms.admissions.common.values;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@EqualsAndHashCode
@Slf4j
public class HashedString {

    private static final int MEMORY = 128000;
    private static final int PARALLELISM = 4;
    private static final int ITERATIONS = 40;

    private static final Argon2 argon2 = Argon2Factory.create();

    @Getter
    private String value;

    private HashedString(String value) {
        this.value = value;
    }

    public static HashedString of(String value) {
        char[] p = value.toCharArray();
        try {
            return new HashedString(argon2.hash(ITERATIONS, MEMORY, PARALLELISM, p));
        } finally {
            argon2.wipeArray(p);
        }
    }

    public static HashedString fromHash(String hash) {
        return new HashedString(hash);
    }

    public boolean isHashOf(String original) {
        char[] p = original.toCharArray();
        try {
            return argon2.verify(this.value, p);
        } finally {
            argon2.wipeArray(p);
        }
    }

}
