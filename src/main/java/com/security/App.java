package com.security;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        // Check if the current system environment is headless (e.g., Docker, Server, Linux Terminal)
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("=============================================================");
            System.out.println("       SECURE FILE INTEGRITY MONITOR - HEADLESS SERVICE      ");
            System.out.println("=============================================================");
            System.out.println("[INFO] Headless environment detected. Gracefully falling back to CLI service.");
            runCliService();
        } else {
            // Desktop environment available - Launch the stunning GUI Dashboard
            try {
                FlatDarkLaf.setup();
            } catch (Exception e) {
                System.err.println("Failed to initialize FlatDarkLaf: " + e.getMessage());
            }

            // Launch the GUI safely on the Event Dispatch Thread (EDT)
            SwingUtilities.invokeLater(() -> {
                new GUI();
            });
        }
    }

    /**
     * Fallback Headless Monitoring Service.
     * Automatically monitors a default file (sensitive.txt) in the background.
     */
    private static void runCliService() {
        try {
            String filePath = "files/sensitive.txt";
            java.io.File file = new java.io.File(filePath);

            // Ensure the target directory and test file exist
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write("CONFIDENTIAL: Secure file integrity monitoring active.");
                }
                System.out.println("[INFO] Default testing file created at: " + file.getAbsolutePath());
            }

            System.out.println("[INFO] Target File: " + file.getName() + " (" + file.length() + " Bytes)");

            // Establish and save initial base hash
            String baseHash = FileHasher.generateHash(filePath);
            System.out.println("[INFO] Base Hash Generated (SHA-256): " + baseHash);
            HashStorage.saveHash(filePath, baseHash);
            System.out.println("[SUCCESS] Integrity baseline established in hashes.txt.");

            System.out.println("\n[SYSTEM] Continuous Integrity Daemon Active (scanning every 5 seconds)...");
            System.out.println("[SYSTEM] Press Ctrl+C in your terminal to stop the service.");
            System.out.println("-------------------------------------------------------------");

            // Continuous polling daemon loop
            while (true) {
                Thread.sleep(5000);
                IntegrityChecker.IntegrityStatus status = IntegrityChecker.verifyFile(filePath);

                switch (status) {
                    case SAFE:
                        System.out.println("[SECURE] File integrity is intact. status: OK");
                        break;
                    case MODIFIED:
                        System.out.println("[CRITICAL ALERT] TAMPER DETECTED! File '" + file.getName() + "' was modified!");
                        break;
                    case FILE_NOT_FOUND:
                        System.out.println("[CRITICAL ALERT] MISSING FILE! Monitored target was deleted!");
                        break;
                    case NO_PREVIOUS_HASH:
                        System.out.println("[WARNING] No baseline signature exists in hashes.txt database.");
                        break;
                    case ERROR:
                        System.out.println("[ERROR] Core engine exception during scan.");
                        break;
                }
            }
        } catch (InterruptedException ie) {
            System.out.println("[SYSTEM] Monitoring daemon interrupted. Exiting.");
        } catch (Exception e) {
            System.err.println("[ERROR] Fatal CLI Service crash: " + e.getMessage());
        }
    }
}