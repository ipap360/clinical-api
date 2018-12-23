package com.timelyworks.clinical.db;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Implements AES (Advanced Encryption Standard) with Galois/Counter Mode (GCM), which is a mode of
 * operation for symmetric key cryptographic block ciphers that has been widely adopted because of
 * its efficiency and performance.
 * <p>
 * Every encryption produces a new 12 byte random IV (see http://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf)
 * because the security of GCM depends choosing a unique initialization vector for every encryption performed with the same key.
 * <p>
 * The iv, encrypted content and auth tag will be encoded to the following format:
 * <p>
 * out = byte[] {x x x x y y y y y y y y y y y y z z z ...}
 * <p>
 * x = IV length as int (4 bytes)
 * y = IV bytes
 * z = content bytes (encrypted content, auth tag)
 *
 * @author Patrick Favre-Bulle
 * @since 18.12.2017
 */
@SuppressWarnings("WeakerAccess")
public final class AesGcmEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final SecureRandom secureRandom;

    public AesGcmEncryption() {
        secureRandom = new SecureRandom();
    }

    public byte[] encrypt(byte[] rawEncryptionKey, byte[] rawData, byte[] associatedData) throws AuthenticatedEncryptionException {
        if (rawEncryptionKey.length < 16) {
            throw new IllegalArgumentException("key length must be longer than 16 bytes");
        }
        byte[] iv = null;
        byte[] encrypted = null;
        try {
            iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(rawEncryptionKey, "AES"), new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }

            encrypted = cipher.doFinal(rawData);

            return ByteBuffer.allocate(4 + iv.length + encrypted.length)
                    .putInt(iv.length)
                    .put(iv)
                    .put(encrypted)
                    .array();
        } catch (Exception e) {
            throw new AuthenticatedEncryptionException("could not encrypt", e);
        } finally {
            safeDelete(iv);
            safeDelete(encrypted);
        }
    }

    public byte[] decrypt(byte[] rawEncryptionKey, byte[] encryptedData, byte[] associatedData) throws AuthenticatedEncryptionException {
        byte[] iv = null;
        byte[] encrypted = null;
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            int ivLength = byteBuffer.getInt();
            if (ivLength < 12 || ivLength >= 16) {
                throw new IllegalArgumentException("invalid iv length");
            }
            encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);
            iv = Arrays.copyOfRange(encrypted, 0, ivLength);

            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(rawEncryptionKey, "AES"), new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }

            return cipher.doFinal(encrypted, ivLength, encrypted.length - ivLength);
        } catch (Exception e) {
            throw new AuthenticatedEncryptionException("could not decrypt", e);
        } finally {
            safeDelete(iv);
            safeDelete(encrypted);
        }
    }

    private void safeDelete(byte[] array) {
        if (array != null) {
            Arrays.fill(array, (byte) 0);
        }
    }

}
