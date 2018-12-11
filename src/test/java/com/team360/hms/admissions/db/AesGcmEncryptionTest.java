package com.team360.hms.admissions.db;

import com.team360.hms.admissions.common.values.RandomToken;
import org.junit.Test;

import java.util.Base64;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class AesGcmEncryptionTest {

    @Test
    public void roundTrip() throws AuthenticatedEncryptionException {

        AesGcmEncryption AES_GCM1 = new AesGcmEncryption();
        AesGcmEncryption AES_GCM2 = new AesGcmEncryption();

        String key = RandomToken.withLength(16).getValue();
        Random rand = new Random();

        final int OUT = 1000;
        final int IN = 100;
        for (int i = 0; i < OUT; i++) {
            String[] values = new String[IN];
            String[] encoded = new String[IN];
            for (int j = 0; j < IN; j++) {
                int n = rand.nextInt(j % 2 == 0 ? 5 : 1000) + 1;
                values[j] = RandomToken.withLength(n).getValue();
                encoded[j] = Base64.getEncoder().encodeToString(AES_GCM1.encrypt(key.getBytes(), values[j].getBytes(), null));
            }
            for (int j = 0; j < IN; j++) {
                String decoded = new String(AES_GCM2.decrypt(key.getBytes(), Base64.getDecoder().decode(encoded[j]), null));
                assertEquals(values[j], decoded);
            }
//            System.out.println(value + ", " + encoded + ", " + decoded);

        }
    }

}