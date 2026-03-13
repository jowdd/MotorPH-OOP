package com.group.motorph.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Central place for all UI colors, fonts, and styling helpers.
 */
public class UITheme {

    // Colors
    public static final Color PRIMARY = new Color(0x2563EB); // main blue #2563EB
    public static final Color PRIMARY_DARK = new Color(0x1D4ED8);
    public static final Color SIDEBAR_BG = new Color(0xF3F3F3);
    public static final Color SIDEBAR_TEXT = new Color(0x333333);
    public static final Color SIDEBAR_ACTIVE = new Color(0x1F6FDB);
    public static final Color SIDEBAR_BORDER = new Color(0xD8D8D8);

    public static final Color BACKGROUND = new Color(0xEFEFEF);
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color PANEL_BORDER = new Color(0xE8E8E8);

    // Table colours
    public static final Color TABLE_HEADER_BG = new Color(0x374151); // neutral gray
    public static final Color TABLE_ROW_EVEN = Color.WHITE;          // #FFFFFF
    public static final Color TABLE_ROW_ODD = new Color(0xF9FAFB);  // #F9FAFB
    public static final Color TABLE_ROW_HOVER = new Color(0xF1F5F9);  // #F1F5F9
    public static final Color TABLE_BORDER = new Color(0xE5E7EB);  // #E5E7EB

    public static final Color TABLE_HEADER = TABLE_HEADER_BG;
    public static final Color TABLE_ALT = TABLE_ROW_ODD;
    public static final Color TABLE_GRID = TABLE_BORDER;

    public static final Color TEXT_DARK = new Color(0x2F3142);
    public static final Color TEXT_MUTED = new Color(0x777777);
    public static final Color TEXT_LIGHT = Color.WHITE;

    public static final Color SUCCESS = new Color(0x22A447);
    public static final Color DANGER = new Color(0xFF3B5C);
    public static final Color WARNING = new Color(0xF5A623);

    // Fonts
    public static final Font FONT_TITLE = new Font("SansSerif", Font.PLAIN, 18);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 11);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_TABLE = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_NAV = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_BRAND = new Font("SansSerif", Font.BOLD, 16);

    public static void setAppLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {
            }
        }

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("Button.font", FONT_BODY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_TABLE);
        UIManager.put("TableHeader.font", FONT_HEADER);
        UIManager.put("TabbedPane.font", FONT_BODY);
    }

    // Buttons
    public static JButton primaryButton(String text) {
        return new RoundedButton(text, PRIMARY, PRIMARY_DARK, TEXT_LIGHT);
    }

    public static JButton dangerButton(String text) {
        return new RoundedButton(text, DANGER, DANGER.darker(), TEXT_LIGHT);
    }

    public static JButton successButton(String text) {
        return new RoundedButton(text, SUCCESS, SUCCESS.darker(), TEXT_LIGHT);
    }

    public static JButton neutralButton(String text) {
        Color neutralBg = new Color(0xD9D9D9);
        return new RoundedButton(text, neutralBg, neutralBg.darker(), TEXT_DARK);
    }

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
        // Hover: lighten bg when not active
        Color navHover = new Color(0xE3E8F0);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVE)) {
                    btn.setBackground(navHover);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVE)) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });
        return btn;
    }

    public static void setNavActive(JButton btn, boolean active) {
        btn.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        btn.setForeground(active ? TEXT_LIGHT : SIDEBAR_TEXT);
    }

    // Tables
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xC3D9FF));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        table.setBackground(TABLE_ROW_EVEN);

        // Dark-slate header
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_LIGHT);
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_BORDER));

        // Custom header renderer
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                lbl.setBackground(TABLE_HEADER_BG);
                lbl.setForeground(TEXT_LIGHT);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
                lbl.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        // Hover-row tracking
        int[] hoveredRow = {-1};
        table.putClientProperty("hoveredRow", hoveredRow);
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow[0]) {
                    hoveredRow[0] = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (hoveredRow[0] != -1) {
                    hoveredRow[0] = -1;
                    table.repaint();
                }
            }
        });

        // Alternating stripe rows + hover
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    setBackground(row == hoveredRow[0] ? TABLE_ROW_HOVER
                            : row % 2 == 0 ? TABLE_ROW_EVEN
                                    : TABLE_ROW_ODD);
                }
                setForeground(TEXT_DARK);
                setBorder(new EmptyBorder(2, 12, 2, 12));
                return this;
            }
        });
    }

    public static Color rowBackground(JTable table, int row, boolean selected) {
        if (selected) {
            return table.getSelectionBackground();
        }
        int[] hr = (int[]) table.getClientProperty("hoveredRow");
        if (hr != null && hr[0] == row) {
            return TABLE_ROW_HOVER;
        }
        return row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD;
    }

    // Sets HAND_CURSOR on the table when the mouse is over any of the specified
    public static void setActionColumns(JTable table, int... actionCols) {
        java.util.Set<Integer> cols = new java.util.HashSet<>();
        for (int c : actionCols) {
            cols.add(c);
        }
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                table.setCursor(row >= 0 && cols.contains(col)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                table.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    public static JScrollPane scrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        // Subtle drop-shadow approximation: matte bottom+right in a near-transparent grey
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 18)),
                BorderFactory.createLineBorder(TABLE_BORDER)
        ));
        sp.getViewport().setBackground(TABLE_ROW_EVEN);
        return sp;
    }

    // Text-link style button for use inside table Action columns.
    @SuppressWarnings("unchecked")
    public static JButton tableActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 4, 0, 4));
        btn.setHorizontalAlignment(JButton.CENTER);
        // Hover: underline + darken
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                @SuppressWarnings("rawtypes")
                Map attrs = new HashMap(btn.getFont().getAttributes());
                attrs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                btn.setFont(btn.getFont().deriveFont(attrs));
                btn.setForeground(PRIMARY_DARK);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                @SuppressWarnings("rawtypes")
                Map attrs = new HashMap(btn.getFont().getAttributes());
                attrs.put(TextAttribute.UNDERLINE, -1);
                btn.setFont(btn.getFont().deriveFont(attrs));
                btn.setForeground(PRIMARY);
            }
        });
        return btn;
    }

    // Labels / headers / cards
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

    // Search field
    public static javax.swing.table.TableCellRenderer centeredHeaderRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                lbl.setBackground(TABLE_HEADER_BG);
                lbl.setForeground(TEXT_LIGHT);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
                lbl.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        };
    }

}
