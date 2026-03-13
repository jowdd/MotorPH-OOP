package com.group.motorph.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 * Modern rounded-corner search input field.
 */
public class RoundedSearchField extends JTextField {

    private static final Color BG_COLOR = new Color(0xF9FAFB);
    private static final Color TEXT_COLOR = new Color(0x374151);
    private static final Color PLACEHOLDER_COLOR = new Color(0x9CA3AF);
    private static final Color BORDER_NORMAL = new Color(0xD1D5DB);
    private static final Color BORDER_HOVER = new Color(0x9CA3AF);
    private static final Color BORDER_FOCUS = new Color(0x2563EB);
    private static final int ARC = 18; // border-radius in pixels

    private boolean hovered = false;
    private final String placeholder;

    // Creates a search field with the given placeholder text.
    public RoundedSearchField(String placeholder) {
        this.placeholder = placeholder;
        init();
    }

    // Creates a search field with the default placeholder "Search".
    public RoundedSearchField() {
        this("Search");
    }

    private void init() {
        setOpaque(false);
        setBackground(BG_COLOR);
        setForeground(TEXT_COLOR);
        setFont(UITheme.FONT_BODY);
        setCaretColor(TEXT_COLOR);

        // Empty border supplies the internal padding (top, left, bottom, right)
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        setPreferredSize(new Dimension(500, 38));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Rounded background
        g2.setColor(BG_COLOR);
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, ARC, ARC));

        // Rounded border
        Color borderColor = isFocusOwner() ? BORDER_FOCUS
                : hovered ? BORDER_HOVER
                        : BORDER_NORMAL;
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1.5f, h - 1.5f, ARC, ARC));
        g2.dispose();

        // Render
        super.paintComponent(g);

        // Placeholder text (drawn after super so it appears over the bg)
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D pg = (Graphics2D) g.create();
            pg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            pg.setColor(PLACEHOLDER_COLOR);
            pg.setFont(getFont());
            Insets ins = getInsets();
            FontMetrics fm = pg.getFontMetrics();
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            pg.drawString(placeholder, ins.left, y);
            pg.dispose();
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }
}
