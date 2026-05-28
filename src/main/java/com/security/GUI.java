package com.security;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GUI extends JFrame {

    // Selected file reference
    private File selectedFile = null;

    // Components
    private JLabel lblFileName;
    private JLabel lblFileSize;
    private JLabel lblLastModified;
    private JTextField txtFilePath;
    private JTextArea txtHashArea;
    
    // Status banner elements
    private JPanel pnlStatusBanner;
    private JLabel lblStatusText;

    // Stats Cards counts
    private JLabel lblStatTotalScansCount;
    private JLabel lblStatSafeCount;
    private JLabel lblStatAlertsCount;
    private int totalScans = 0;
    private int safeScans = 0;
    private int alertScans = 0;

    // Log terminal
    private JTextArea txtLogTerminal;

    // Buttons
    private JButton btnBrowse;
    private JButton btnGenerateHash;
    private JButton btnVerifyIntegrity;
    private JButton btnCopyHash;
    private JToggleButton btnRealTimeToggle;

    // Real-Time Background Monitor
    private Timer backgroundTimer;
    private static final int MONITOR_INTERVAL_MS = 2500; // scan every 2.5 seconds
    private String lastVerifiedHash = null;

    // Theme Colors
    private static final Color COLOR_BG = new Color(0x12, 0x16, 0x20);       // Rich dark slate
    private static final Color COLOR_CARD_BG = new Color(0x1E, 0x25, 0x33);  // Slightly lighter elevation
    private static final Color COLOR_TEXT_PRIMARY = new Color(0xF1, 0xF5, 0xF9);
    private static final Color COLOR_TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);
    private static final Color COLOR_ACCENT = new Color(0x3B, 0x82, 0xF6);      // Electric Blue
    private static final Color COLOR_SUCCESS = new Color(0x10, 0xB9, 0x81);     // Emerald Green
    private static final Color COLOR_DANGER = new Color(0xEF, 0x44, 0x44);      // Vibrant Crimson
    private static final Color COLOR_WARNING = new Color(0xF5, 0x9E, 0x0B);     // Bright Amber

    public GUI() {
        // Set Frame configuration
        setTitle("SECURE FILE INTEGRITY MONITOR - ENTERPRISE DASHBOARD");
        setSize(960, 720);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout(15, 15));

        // Create main UI parts
        initHeaderPanel();
        initCenterWorkspacePanel();
        initActivityLogPanel();

        // Register window drag-and-drop listener on the entire window frame
        setupDragAndDropSupport((JComponent) this.getContentPane());

        // Initialize Background Polling Timer for Real-Time Watch Service
        initRealTimeMonitor();

        // Print initial welcome log
        log("System initialized. FlatDark theme applied successfully.");
        log("Drag and drop any file or click 'Browse System' to get started.");

        setVisible(true);
    }

    /**
     * Header Section: Title, Sub-branding, and KPI Statistics Cards.
     */
    private void initHeaderPanel() {
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(COLOR_BG);
        pnlHeader.setLayout(new BorderLayout(10, 10));
        pnlHeader.setBorder(new EmptyBorder(15, 15, 0, 15));

        // Title and Subtitle block
        JPanel pnlTitle = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlTitle.setBackground(COLOR_BG);

        JLabel lblTitle = new JLabel("SECURE INTEGRITY AUDITOR");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Real-time cryptographic verification with SHA-256 intrusion detection");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(COLOR_ACCENT);

        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);
        pnlHeader.add(pnlTitle, BorderLayout.WEST);

        // Stats Panel (Horizontal Cards)
        JPanel pnlStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlStats.setBackground(COLOR_BG);

        pnlStats.add(createStatCard("TOTAL SCANS", "0", COLOR_ACCENT));
        pnlStats.add(createStatCard("SAFE CHECKS", "0", COLOR_SUCCESS));
        pnlStats.add(createStatCard("TAMPER ALERTS", "0", COLOR_DANGER));

        pnlHeader.add(pnlStats, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);
    }

    /**
     * Helper to create a highly styled stat card.
     */
    private JPanel createStatCard(String title, String initialVal, Color accentColor) {
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(140, 65));
        card.setBackground(COLOR_CARD_BG);
        card.setLayout(new BorderLayout(2, 2));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BG.brighter(), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        JLabel lblValue = new JLabel(initialVal);
        lblValue.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        lblValue.setForeground(COLOR_TEXT_PRIMARY);

        // Underline effect
        JPanel accentLine = new JPanel();
        accentLine.setPreferredSize(new Dimension(140, 3));
        accentLine.setBackground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(accentLine, BorderLayout.SOUTH);

        // Cache the value labels to update dynamically
        if (title.equals("TOTAL SCANS")) {
            lblStatTotalScansCount = lblValue;
        } else if (title.equals("SAFE CHECKS")) {
            lblStatSafeCount = lblValue;
        } else if (title.equals("TAMPER ALERTS")) {
            lblStatAlertsCount = lblValue;
        }

        return card;
    }

    /**
     * Center Workspace: File upload, metadata, Actions, status panel, and hash panel.
     */
    private void initCenterWorkspacePanel() {
        JPanel pnlWorkspace = new JPanel(new GridBagLayout());
        pnlWorkspace.setBackground(COLOR_BG);
        pnlWorkspace.setBorder(new EmptyBorder(10, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 10, 0);

        // --- 1. DYNAMIC FILE INPUT & DROP AREA ---
        JPanel pnlDropZone = new JPanel(new GridBagLayout());
        pnlDropZone.setBackground(COLOR_CARD_BG);
        pnlDropZone.setPreferredSize(new Dimension(0, 85));
        pnlDropZone.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(COLOR_ACCENT, 1.5f, 6.0f, 4.0f, false),
                new EmptyBorder(10, 15, 10, 15)
        ));
        setupDragAndDropSupport(pnlDropZone);

        GridBagConstraints gbcDrop = new GridBagConstraints();
        gbcDrop.gridx = 0;
        gbcDrop.gridy = 0;
        gbcDrop.weightx = 1.0;
        gbcDrop.anchor = GridBagConstraints.WEST;

        JPanel pnlDropText = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlDropText.setBackground(COLOR_CARD_BG);
        JLabel lblDropMain = new JLabel("DRAG & DROP SECURE FILES");
        lblDropMain.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        lblDropMain.setForeground(COLOR_TEXT_PRIMARY);
        JLabel lblDropSub = new JLabel("Drop any file from your computer to analyze and monitor integrity");
        lblDropSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDropSub.setForeground(COLOR_TEXT_MUTED);
        pnlDropText.add(lblDropMain);
        pnlDropText.add(lblDropSub);
        pnlDropZone.add(pnlDropText, gbcDrop);

        gbcDrop.gridx = 1;
        gbcDrop.weightx = 0.0;
        gbcDrop.anchor = GridBagConstraints.EAST;
        btnBrowse = new JButton("Browse System");
        btnBrowse.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        btnBrowse.setFocusPainted(false);
        btnBrowse.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBrowse.addActionListener(e -> chooseFile());
        pnlDropZone.add(btnBrowse, gbcDrop);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        pnlWorkspace.add(pnlDropZone, gbc);

        // --- 2. FILE DETAILS & METADATA CARD ---
        JPanel pnlFileDetails = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlFileDetails.setBackground(COLOR_BG);

        // Detailed elements cached
        lblFileName = new JLabel("Name: No File Selected");
        lblFileName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFileName.setForeground(COLOR_TEXT_PRIMARY);

        lblFileSize = new JLabel("Size: 0 Bytes");
        lblFileSize.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFileSize.setForeground(COLOR_TEXT_PRIMARY);

        lblLastModified = new JLabel("Last Modified: N/A");
        lblLastModified.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLastModified.setForeground(COLOR_TEXT_PRIMARY);

        txtFilePath = new JTextField("Path: None");
        txtFilePath.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        txtFilePath.setForeground(COLOR_TEXT_MUTED);
        txtFilePath.setEditable(false);
        txtFilePath.setBorder(null);
        txtFilePath.setBackground(COLOR_BG);

        pnlFileDetails.add(lblFileName);
        pnlFileDetails.add(lblFileSize);
        pnlFileDetails.add(lblLastModified);
        pnlFileDetails.add(txtFilePath);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 5, 10, 5);
        pnlWorkspace.add(pnlFileDetails, gbc);

        // --- 3. STATUS INDICATOR BANNER ---
        pnlStatusBanner = new JPanel(new BorderLayout());
        pnlStatusBanner.setPreferredSize(new Dimension(0, 50));
        pnlStatusBanner.setBackground(COLOR_WARNING);
        pnlStatusBanner.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        lblStatusText = new JLabel("SYSTEM STATUS: NO ACTIVE FILE SELECTED");
        lblStatusText.setFont(new Font("Segoe UI Black", Font.BOLD, 14));
        lblStatusText.setForeground(Color.BLACK);
        lblStatusText.setHorizontalAlignment(SwingConstants.CENTER);
        pnlStatusBanner.add(lblStatusText, BorderLayout.CENTER);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        pnlWorkspace.add(pnlStatusBanner, gbc);

        // --- 4. HASH DETAILS AREA ---
        JPanel pnlHash = new JPanel(new BorderLayout(10, 0));
        pnlHash.setBackground(COLOR_BG);
        
        JLabel lblHashTitle = new JLabel("SHA-256 HASH SIGNATURE");
        lblHashTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
        lblHashTitle.setForeground(COLOR_TEXT_MUTED);
        pnlHash.add(lblHashTitle, BorderLayout.NORTH);

        txtHashArea = new JTextArea("Click 'Generate Hash' to calculate cryptography...");
        txtHashArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtHashArea.setForeground(COLOR_TEXT_MUTED);
        txtHashArea.setBackground(COLOR_CARD_BG);
        txtHashArea.setEditable(false);
        txtHashArea.setLineWrap(true);
        txtHashArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BG.brighter(), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));
        pnlHash.add(txtHashArea, BorderLayout.CENTER);

        btnCopyHash = new JButton("Copy Hash");
        btnCopyHash.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
        btnCopyHash.setEnabled(false);
        btnCopyHash.setFocusPainted(false);
        btnCopyHash.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCopyHash.addActionListener(e -> copyHashToClipboard());
        
        JPanel pnlCopyBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        pnlCopyBtnWrap.setBackground(COLOR_BG);
        pnlCopyBtnWrap.add(btnCopyHash);
        pnlHash.add(pnlCopyBtnWrap, BorderLayout.SOUTH);

        gbc.gridy = 3;
        pnlWorkspace.add(pnlHash, gbc);

        // --- 5. ACTION BUTTON CONTROLS ---
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlControls.setBackground(COLOR_BG);

        btnGenerateHash = new JButton("Generate Hash");
        btnGenerateHash.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnGenerateHash.setEnabled(false);
        btnGenerateHash.setFocusPainted(false);
        btnGenerateHash.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGenerateHash.addActionListener(e -> generateHash());

        btnVerifyIntegrity = new JButton("Verify Integrity");
        btnVerifyIntegrity.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnVerifyIntegrity.setEnabled(false);
        btnVerifyIntegrity.setFocusPainted(false);
        btnVerifyIntegrity.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerifyIntegrity.addActionListener(e -> verifyFile());

        btnRealTimeToggle = new JToggleButton("Real-Time Watcher: OFF");
        btnRealTimeToggle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btnRealTimeToggle.setEnabled(false);
        btnRealTimeToggle.setFocusPainted(false);
        btnRealTimeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRealTimeToggle.addActionListener(e -> toggleRealTimeMonitor());

        pnlControls.add(btnGenerateHash);
        pnlControls.add(btnVerifyIntegrity);
        pnlControls.add(btnRealTimeToggle);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 5, 0);
        pnlWorkspace.add(pnlControls, gbc);

        add(pnlWorkspace, BorderLayout.CENTER);
    }

    /**
     * Bottom Activity Log Terminal Area.
     */
    private void initActivityLogPanel() {
        JPanel pnlActivity = new JPanel(new BorderLayout(5, 5));
        pnlActivity.setBackground(COLOR_BG);
        pnlActivity.setPreferredSize(new Dimension(0, 180));
        pnlActivity.setBorder(new EmptyBorder(0, 15, 15, 15));

        JLabel lblActivityTitle = new JLabel("ACTIVITY LOG & INTRUSION DETECTION CONSOLE");
        lblActivityTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
        lblActivityTitle.setForeground(COLOR_TEXT_MUTED);
        pnlActivity.add(lblActivityTitle, BorderLayout.NORTH);

        txtLogTerminal = new JTextArea();
        txtLogTerminal.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLogTerminal.setForeground(new Color(0x38, 0xEF, 0x7D)); // Neon Green terminal look
        txtLogTerminal.setBackground(Color.BLACK);
        txtLogTerminal.setEditable(false);
        txtLogTerminal.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollLog = new JScrollPane(txtLogTerminal);
        scrollLog.setBorder(new LineBorder(COLOR_CARD_BG, 1));
        pnlActivity.add(scrollLog, BorderLayout.CENTER);

        add(pnlActivity, BorderLayout.SOUTH);
    }

    /**
     * File browse handler.
     */
    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select File to Monitor");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectFile(chooser.getSelectedFile());
        }
    }

    /**
     * Unified file loader, triggered by browse button or Drag and Drop.
     */
    private void selectFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Please select a valid, existing file.", "Invalid File", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Stop active background watcher if switching files
        if (btnRealTimeToggle.isSelected()) {
            btnRealTimeToggle.setSelected(false);
            toggleRealTimeMonitor();
        }

        selectedFile = file;
        lastVerifiedHash = null;

        // Extract metadata
        String name = file.getName();
        String path = file.getAbsolutePath();
        long bytes = file.length();
        String sizeFormatted = formatFileSize(bytes);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String modifiedFormatted = sdf.format(new Date(file.lastModified()));

        // Update details labels
        lblFileName.setText("Name: " + name);
        lblFileSize.setText("Size: " + sizeFormatted);
        lblLastModified.setText("Last Modified: " + modifiedFormatted);
        txtFilePath.setText(path);

        // Reset hash text and copy button
        txtHashArea.setText("Click 'Generate Hash' to calculate SHA-256 for the new file.");
        txtHashArea.setForeground(COLOR_TEXT_MUTED);
        btnCopyHash.setEnabled(false);

        // Adjust banner status
        pnlStatusBanner.setBackground(COLOR_WARNING);
        lblStatusText.setText("SYSTEM STATUS: FILE LOADED - HASH NOT YET GENERATED");
        lblStatusText.setForeground(Color.BLACK);

        // Enable buttons
        btnGenerateHash.setEnabled(true);
        btnVerifyIntegrity.setEnabled(true);
        btnRealTimeToggle.setEnabled(true);

        log("Successfully selected file: " + name + " (" + sizeFormatted + ")");
    }

    /**
     * Generates a hash, displays it, and stores it in hashes.txt.
     */
    private void generateHash() {
        if (selectedFile == null) return;
        
        try {
            log("Calculating SHA-256 hash digest for: " + selectedFile.getName());
            String hash = FileHasher.generateHash(selectedFile.getAbsolutePath());
            
            txtHashArea.setText(hash);
            txtHashArea.setForeground(COLOR_TEXT_PRIMARY);
            btnCopyHash.setEnabled(true);

            // Save to storage
            HashStorage.saveHash(selectedFile.getAbsolutePath(), hash);
            lastVerifiedHash = hash;

            // Update GUI Stats
            totalScans++;
            lblStatTotalScansCount.setText(String.valueOf(totalScans));

            // Set secure status banner
            pnlStatusBanner.setBackground(COLOR_SUCCESS);
            lblStatusText.setText("STATUS: HASH STORED SUCCESSFULLY - FILE PROTECTED");
            lblStatusText.setForeground(COLOR_TEXT_PRIMARY);

            log("Success: Hash stored in 'hashes.txt'. File protected.");
        } catch (Exception e) {
            log("Error generating hash: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to generate hash: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Manually triggers file integrity check.
     */
    private void verifyFile() {
        if (selectedFile == null) return;
        
        log("Running manual integrity scan on: " + selectedFile.getName());
        
        totalScans++;
        lblStatTotalScansCount.setText(String.valueOf(totalScans));

        IntegrityChecker.IntegrityStatus status = IntegrityChecker.verifyFile(selectedFile.getAbsolutePath());

        switch (status) {
            case SAFE:
                safeScans++;
                lblStatSafeCount.setText(String.valueOf(safeScans));
                pnlStatusBanner.setBackground(COLOR_SUCCESS);
                lblStatusText.setText("STATUS: INTEGRITY SECURE - FILE SAFE");
                lblStatusText.setForeground(COLOR_TEXT_PRIMARY);
                log("Verification Success: Integrity checked. File is secure and untampered.");
                break;
            case MODIFIED:
                alertScans++;
                lblStatAlertsCount.setText(String.valueOf(alertScans));
                pnlStatusBanner.setBackground(COLOR_DANGER);
                lblStatusText.setText("ALERT: INTRUSION DETECTED! FILE WAS MODIFIED!");
                lblStatusText.setForeground(COLOR_TEXT_PRIMARY);
                log("WARNING ALERT: File modification detected! SHA-256 signatures do not match!");
                break;
            case NO_PREVIOUS_HASH:
                pnlStatusBanner.setBackground(COLOR_WARNING);
                lblStatusText.setText("STATUS: INTEGRITY UNKNOWN - NO HASH FOUND IN DATABASE");
                lblStatusText.setForeground(Color.BLACK);
                log("Verification Warning: No previous hash signature found in hashes.txt database.");
                break;
            case FILE_NOT_FOUND:
                pnlStatusBanner.setBackground(COLOR_DANGER);
                lblStatusText.setText("ALERT: MONITORING FAILED - FILE DELETED OR MOVED!");
                lblStatusText.setForeground(COLOR_TEXT_PRIMARY);
                log("CRITICAL ERROR: Monitored file could not be found at destination path.");
                break;
            case ERROR:
                pnlStatusBanner.setBackground(COLOR_DANGER);
                lblStatusText.setText("STATUS: CHECK COMPLETED WITH INTERNAL SYSTEM ERROR");
                lblStatusText.setForeground(COLOR_TEXT_PRIMARY);
                log("Error: System failed to execute hash check safely.");
                break;
        }
    }

    /**
     * Initialize background timer for real-time file watch scanning.
     */
    private void initRealTimeMonitor() {
        backgroundTimer = new Timer(MONITOR_INTERVAL_MS, e -> {
            if (selectedFile == null || !selectedFile.exists()) {
                pnlStatusBanner.setBackground(COLOR_DANGER);
                lblStatusText.setText("ALERT: REAL-TIME WATCHER DETECTED TARGET MISSING!");
                lblStatusText.setForeground(COLOR_TEXT_PRIMARY);
                log("Real-Time Watcher Alarm: Target file is no longer accessible!");
                btnRealTimeToggle.setSelected(false);
                btnRealTimeToggle.setText("Real-Time Watcher: OFF");
                backgroundTimer.stop();
                return;
            }

            // Run check silently
            IntegrityChecker.IntegrityStatus status = IntegrityChecker.verifyFile(selectedFile.getAbsolutePath());
            if (status == IntegrityChecker.IntegrityStatus.SAFE) {
                // If it was modified before and now matches original again, or just steady
                pnlStatusBanner.setBackground(COLOR_SUCCESS);
                lblStatusText.setText("STATUS: REAL-TIME SECURED - FILE SAFE");
                lblStatusText.setForeground(COLOR_TEXT_PRIMARY);
            } else if (status == IntegrityChecker.IntegrityStatus.MODIFIED) {
                // Instantly update badge and sound warning
                pnlStatusBanner.setBackground(COLOR_DANGER);
                lblStatusText.setText("CRITICAL ALERT: LIVE TAMPER DETECTION TRIGGERED!");
                lblStatusText.setForeground(COLOR_TEXT_PRIMARY);
                
                // Track stats once upon first discovery of change
                try {
                    String currentHash = FileHasher.generateHash(selectedFile.getAbsolutePath());
                    if (lastVerifiedHash == null || !currentHash.equalsIgnoreCase(lastVerifiedHash)) {
                        alertScans++;
                        lblStatAlertsCount.setText(String.valueOf(alertScans));
                        lastVerifiedHash = currentHash;
                        log("CRITICAL LIVE ALERT: Real-time scan detected modification to: " + selectedFile.getName());
                    }
                } catch (Exception ex) {
                    // ignore
                }
            }
        });
    }

    /**
     * Toggles the background real-time watch thread.
     */
    private void toggleRealTimeMonitor() {
        if (selectedFile == null) return;

        if (btnRealTimeToggle.isSelected()) {
            btnRealTimeToggle.setText("Real-Time Watcher: ACTIVE");
            btnRealTimeToggle.setForeground(COLOR_SUCCESS);
            log("Background Watcher activated. Scanning for changes every " + (MONITOR_INTERVAL_MS / 1000.0) + " seconds...");
            
            // Generate initial hash if not present in history before starting
            try {
                if (HashStorage.readHash(selectedFile.getAbsolutePath()) == null) {
                    generateHash();
                }
            } catch (Exception ex) {
                // ignore
            }

            backgroundTimer.start();
        } else {
            btnRealTimeToggle.setText("Real-Time Watcher: OFF");
            btnRealTimeToggle.setForeground(COLOR_TEXT_PRIMARY);
            log("Background Watcher deactivated.");
            backgroundTimer.stop();
            verifyFile(); // Re-verify once to align status
        }
    }

    /**
     * Copies active SHA-256 text to clipboard.
     */
    private void copyHashToClipboard() {
        String hash = txtHashArea.getText();
        if (hash != null && hash.length() == 64) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(hash), null);
            log("Hash signature copied to system clipboard successfully.");
        }
    }

    /**
     * Appends a message to the logging console with timestamp.
     */
    private void log(String msg) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        txtLogTerminal.append("[" + timeStamp + "] " + msg + "\n");
        // Auto-scroll
        txtLogTerminal.setCaretPosition(txtLogTerminal.getDocument().getLength());
    }

    /**
     * File size formatter utility.
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " Bytes";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Sets up TransferHandler on components to handle Drag & Drop file importing natively.
     */
    private void setupDragAndDropSupport(JComponent component) {
        component.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files != null && !files.isEmpty()) {
                        // Load the first dropped file
                        selectFile(files.get(0));
                        return true;
                    }
                } catch (Exception e) {
                    log("Drag-and-Drop failed: " + e.getMessage());
                }
                return false;
            }
        });
    }
}
