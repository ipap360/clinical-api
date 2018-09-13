package common.values;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.text.RandomStringGenerator;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@ToString
@EqualsAndHashCode
public class RandomToken {

    private static final RandomStringGenerator generator = new RandomStringGenerator.Builder()
//                .withinRange('!', '~')
            .withinRange('0', 'z')
            .filteredBy(LETTERS, DIGITS)
//		     .usingRandom(rng::nextInt) // uses Java 8 syntax
            .build();
    @Getter
    private String value;

    private RandomToken(int length) {
        this.value = generator.generate(length);
    }

    public static RandomToken withLength(int length) {
        return new RandomToken(length);
    }

}
