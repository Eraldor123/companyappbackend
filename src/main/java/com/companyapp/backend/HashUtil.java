package com.companyapp.backend;

public class HashUtil {

    private static final String SALT = "s0m3R@nd0mS@lt";

    public static String hash(String input) {
        // Sha256 hashing implementation
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean verify(String input, String hash) {
        return hash(input).equals(hash);
    }

    public static String hashWithSalt(String input) {
        return hash(input + SALT);
    }
}
