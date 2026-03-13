package com.group.motorph.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility for hashing and verifying passwords using PBKDF2WithHmacSHA256 with a
 * random salt. Stored format: PBKDF2:iterations:saltHex:hashHex
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "PBKDF2";
    private static final int ITERATIONS = 100_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;

    private PasswordUtil() {
    }

    // Returns true if the stored value looks like a hashed password.
    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX + ":");
    }

    /**
     * Hashes a plain-text password into the persisted format
     * {@code PBKDF2:iterations:saltHex:hashHex}.
     *
     * A new random salt is generated for every password so two identical
     * passwords will not produce the same stored string.
     */
    public static String hash(String plainPassword) {
        try {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_BITS);
            HexFormat hex = HexFormat.of();
            return PREFIX + ":" + ITERATIONS + ":" + hex.formatHex(salt) + ":" + hex.formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verifies a plain-text password against either a PBKDF2 hash or a legacy
     * plain-text value.
     *
     * The plain-text branch exists only for backward compatibility during the
     * migration period. Callers such as {@code UserDAOImpl.authenticate()} are
     * expected to re-hash successful legacy logins immediately.
     */
    public static boolean verify(String plainPassword, String stored) {
        if (!isHashed(stored)) {
            // Legacy plain-text comparison (triggers migration path)
            return stored != null && stored.equals(plainPassword);
        }
        try {
            String[] parts = stored.split(":");
            if (parts.length != 4) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            HexFormat hex = HexFormat.of();
            byte[] salt = hex.parseHex(parts[2]);
            byte[] expected = hex.parseHex(parts[3]);
            byte[] actual = pbkdf2(plainPassword.toCharArray(), salt, iterations, expected.length * 8);
            return slowEquals(expected, actual);
        } catch (NumberFormatException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
    }

    // Internal helpers
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bits)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Compares two byte arrays in near-constant time.
     *
     * Avoiding early-exit equality checks makes it harder to infer how many
     * bytes matched from response timing alone.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
