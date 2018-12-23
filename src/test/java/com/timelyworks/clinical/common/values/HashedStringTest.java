package com.timelyworks.clinical.common.values;

import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class HashedStringTest {

    static Random rand;

    @BeforeClass
    public static void setUp() throws Exception {
        rand = new Random();
    }

    @Test
    public void all() {
        for (int i = 0; i < 2; i++) {
            System.out.println("test " + i);
            int len = 50; // rand.nextInt(100) + 1;
            String str = RandomToken.withLength(len).getValue();
//            System.out.println("test string: " + str);
            Instant start = Instant.now();
            String hash = HashedString.of(str).getValue();
//            System.out.printf("Hashing completed in %dms%n", ChronoUnit.MILLIS.between(start, Instant.now()));
            HashedString test = HashedString.fromHash(hash);
            assertTrue(test.isHashOf(str));
        }
    }

}