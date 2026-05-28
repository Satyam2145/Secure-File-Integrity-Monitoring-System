package com.security;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HashStorage {

    private static final String FILE_NAME = "hashes.txt";

    /**
     * Reads all hashes from the storage file into a Map.
     * Uses a robust parsing mechanism that supports Windows absolute paths (containing 'C:')
     * by splitting on the final colon character.
     */
    private static Map<String, String> loadAllHashes() {
        Map<String, String> hashes = new HashMap<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return hashes;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                int lastColon = line.lastIndexOf(':');
                // The hash is a 64-char hex string, so lastColon should be valid
                if (lastColon > 0 && lastColon < line.length() - 1) {
                    String fileIdentifier = line.substring(0, lastColon);
                    String hash = line.substring(lastColon + 1);
                    hashes.put(fileIdentifier, hash);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading hashes: " + e.getMessage());
        }
        return hashes;
    }

    /**
     * Saves or updates the SHA-256 hash for a given file identifier (path or name).
     * Overwrites previous entries to keep the storage clean and accurate.
     *
     * @param fileIdentifier Unique key for the file (preferably absolute path)
     * @param hash SHA-256 digest string
     * @throws Exception if writing to file fails
     */
    public static synchronized void saveHash(String fileIdentifier, String hash) throws Exception {
        Map<String, String> hashes = loadAllHashes();
        hashes.put(fileIdentifier, hash);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, String> entry : hashes.entrySet()) {
                bw.write(entry.getKey() + ":" + entry.getValue());
                bw.newLine();
            }
        }
    }

    /**
     * Retrieves the stored hash for a given file identifier.
     *
     * @param fileIdentifier Unique key for the file
     * @return The stored hash string, or null if not found
     * @throws Exception if reading fails
     */
    public static String readHash(String fileIdentifier) throws Exception {
        Map<String, String> hashes = loadAllHashes();
        return hashes.get(fileIdentifier);
    }
}
