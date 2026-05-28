package com.security;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class FileHasher {

    /**
     * Generates a SHA-256 hash for the given file path.
     * Uses a chunked input stream buffer to handle files of any size safely without high memory usage.
     *
     * @param filePath Absolute or relative path to the file
     * @return Hexadecimal representation of the SHA-256 hash
     * @throws Exception if file is not found or hashing fails
     */
    public static String generateHash(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        
        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }
}
