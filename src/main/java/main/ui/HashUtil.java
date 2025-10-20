package main.ui;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class HashUtil {
    private HashUtil() {}

    public static String generateSalt() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    public static String hashWithSalt(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes("UTF-8"));
            byte[] digest = md.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
