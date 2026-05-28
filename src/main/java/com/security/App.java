package com.security;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        // Initialize the premium FlatLaf Dark theme Look & Feel
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