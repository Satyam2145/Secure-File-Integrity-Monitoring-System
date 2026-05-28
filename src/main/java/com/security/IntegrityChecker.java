package com.security;

import java.io.File;

public class IntegrityChecker {

    public enum IntegrityStatus {
        SAFE,
        MODIFIED,
        NO_PREVIOUS_HASH,
        FILE_NOT_FOUND,
        ERROR
    }

    /**
     * Verifies the integrity of a file by comparing its current SHA-256 hash
     * with the hash stored in hashes.txt.
     *
     * @param filePath Absolute path of the file to verify
     * @return IntegrityStatus representing the state of the file
     */
    public static IntegrityStatus verifyFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return IntegrityStatus.FILE_NOT_FOUND;
            }

            // Generate active hash of the file
            String currentHash = FileHasher.generateHash(filePath);

            // Read the stored hash from storage using absolute path as key
            String storedHash = HashStorage.readHash(filePath);
            
            // Fallback to checking by filename if absolute path is not stored (for legacy or manual edits)
            if (storedHash == null) {
                storedHash = HashStorage.readHash(file.getName());
            }

            if (storedHash == null) {
                return IntegrityStatus.NO_PREVIOUS_HASH;
            }

            if (currentHash.equalsIgnoreCase(storedHash)) {
                return IntegrityStatus.SAFE;
            } else {
                return IntegrityStatus.MODIFIED;
            }

        } catch (Exception e) {
            System.err.println("Verification error for " + filePath + ": " + e.getMessage());
            return IntegrityStatus.ERROR;
        }
    }
}
