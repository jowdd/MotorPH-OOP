package com.group.motorph.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

/**
 * Centralized theme tokens for the MotorPH desktop app.
 * Keeps colors, fonts, and simple component helpers consistent across screens.
 */

public final class Theme {
    private Theme() {}

    // Palette
    public static final Color BACKGROUND = new Color(0xF6F7F9);
    public static final Color SURFACE = new Color(0xFFFFFF);
    public static final Color BORDER = new Color(0xE3E7ED);
    public static final Color TEXT_PRIMARY = new Color(0x1F2933);
    public static final Color TEXT_MUTED = new Color(0x5A6B7B);
    public static final Color ACCENT = new Color(0x2D6AE3);
    public static final Color ACCENT_DARK = new Color(0x1F4CA8);
    public static final Color SUCCESS = new Color(0x1EA97C);
    public static final Color DANGER = new Color(0xDA3B3B);
    public static final Color WARNING = new Color(0xF59E0B);

    // Typography
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    static {
        UIManager.put("Button.disabledText", Color.WHITE);
    }

    /**
     * Creates a pill-shaped button with shared styling.
     */
    public static JButton createButton(String label, Color background) {
        JButton button = new JButton(label);
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        return button;
    }

    /**
     * Styles a neutral secondary button with white text.
     */
    public static JButton createGhostButton(String label) {
        JButton button = new JButton(label);
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setBackground(new Color(0xEDF1F7));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return button;
    }

    /**
     * Wraps a panel with standard padding and background for cards.
     */
    public static JPanel cardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 16, 14, 16)));
        return panel;
    }

    /**
     * Applies subtle styling to tables with modern header.
     */
    public static void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xE8F0FE));
        table.setSelectionForeground(TEXT_PRIMARY);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(ACCENT);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
    }


}
