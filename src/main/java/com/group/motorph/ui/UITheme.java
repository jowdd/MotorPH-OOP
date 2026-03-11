package com.group.motorph.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Central place for all UI colors, fonts, and styling helpers.
 * Updated to better match the provided UI mockups.
 */
public class UITheme {

    // --- Colors based on mockups ---
    public static final Color PRIMARY         = new Color(0x1F6FDB); // main blue
    public static final Color PRIMARY_DARK    = new Color(0x185EC0);
    public static final Color SIDEBAR_BG      = new Color(0xF3F3F3);
    public static final Color SIDEBAR_TEXT    = new Color(0x333333);
    public static final Color SIDEBAR_ACTIVE  = new Color(0x1F6FDB);
    public static final Color SIDEBAR_BORDER  = new Color(0xD8D8D8);

    public static final Color BACKGROUND      = new Color(0xEFEFEF);
    public static final Color PANEL_BG        = Color.WHITE;
    public static final Color PANEL_BORDER    = new Color(0xE8E8E8);

    public static final Color TABLE_HEADER    = new Color(0xF1F1F1);
    public static final Color TABLE_ALT       = Color.WHITE;
    public static final Color TABLE_GRID      = new Color(0xECECEC);

    public static final Color TEXT_DARK       = new Color(0x2F3142);
    public static final Color TEXT_MUTED      = new Color(0x777777);
    public static final Color TEXT_LIGHT      = Color.WHITE;

    public static final Color SUCCESS         = new Color(0x22A447);
    public static final Color DANGER          = new Color(0xFF3B5C);
    public static final Color WARNING         = new Color(0xF5A623);

    // --- Fonts ---
    public static final Font FONT_TITLE       = new Font("SansSerif", Font.PLAIN, 18);
    public static final Font FONT_HEADER      = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_BODY        = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SMALL       = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_NAV         = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_BRAND       = new Font("SansSerif", Font.BOLD, 16);

    public static void setAppLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("Button.font", FONT_BODY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_HEADER);
        UIManager.put("TabbedPane.font", FONT_BODY);
    }

    // ------------------------------------------------------------------
    // Buttons
    // ------------------------------------------------------------------

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        styleActionButton(btn, PRIMARY, TEXT_LIGHT);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        styleActionButton(btn, DANGER, TEXT_LIGHT);
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        styleActionButton(btn, SUCCESS, TEXT_LIGHT);
        return btn;
    }

    public static JButton neutralButton(String text) {
        JButton btn = new JButton(text);
        styleActionButton(btn, new Color(0xD9D9D9), TEXT_DARK);
        return btn;
    }

    private static void styleActionButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 14, 8, 14));
    }

    // ------------------------------------------------------------------
    // Sidebar nav button
    // ------------------------------------------------------------------

    public static JButton navButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(SIDEBAR_TEXT);
        btn.setFont(FONT_NAV);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return btn;
    }

    public static void setNavActive(JButton btn, boolean active) {
        btn.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        btn.setForeground(active ? TEXT_LIGHT : SIDEBAR_TEXT);
    }

    // ------------------------------------------------------------------
    // Tables
    // ------------------------------------------------------------------

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xDCE9FF));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(TEXT_DARK);
        header.setFont(FONT_HEADER);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createLineBorder(TABLE_GRID));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) setBackground(TABLE_ALT);
                setForeground(TEXT_DARK);
                setBorder(new EmptyBorder(2, 8, 2, 8));
                return this;
            }
        });
    }

    public static JScrollPane scrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ------------------------------------------------------------------
    // Labels / headers / cards
    // ------------------------------------------------------------------

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    public static JPanel pagePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BACKGROUND);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        return p;
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PANEL_BORDER),
                new EmptyBorder(14, 14, 14, 14)
        ));
        return p;
    }

    public static JPanel sectionHeader(String text) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BACKGROUND);
        wrap.setBorder(new EmptyBorder(12, 16, 10, 16));

        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_DARK);

        wrap.add(lbl, BorderLayout.WEST);
        return wrap;
    }
}