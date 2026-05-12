package com.security;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class App {

    public static String generateHash(String filePath) throws Exception {

        byte[] fileData = Files.readAllBytes(Paths.get(filePath));

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hashBytes = md.digest(fileData);

        StringBuilder sb = new StringBuilder();

        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        try {

            String filePath = "files/sensitive.txt";

            System.out.println("Monitoring file...");

            String originalHash = generateHash(filePath);

            System.out.println("Original Hash:");
            System.out.println(originalHash);

            System.out.println("\nRunning integrity check...");

            String currentHash = generateHash(filePath);

            if (originalHash.equals(currentHash)) {
                System.out.println("Status: File Secure");
            } else {
                System.out.println("Alert: File Tampering Detected!");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}