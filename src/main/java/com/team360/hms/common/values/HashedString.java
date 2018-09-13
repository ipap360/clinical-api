package common.values;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class HashedString {

    // private static final int MSECS = 1000;

    private static final int MEMORY = 65536;
    private static final int PARALLELISM = 4;
    private static final int ITERATIONS = 40;

    private static final Argon2 argon2 = Argon2Factory.create();

//    private static int ITERATIONS = Argon2Helper.findIterations(argon2, MSECS, MEMORY, PARALLELISM);
//
//    static {
//        if (ITERATIONS <= 3) {
//            ITERATIONS = 4;
//        }
//        log.info("Argon2: " + ITERATIONS);
//    }

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
