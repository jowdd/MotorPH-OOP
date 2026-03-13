package com.group.motorph.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * A JPanel that paints a rounded rectangle background and border instead of the
 * default rectangular fill.
 */
public class RoundedPanel extends JPanel {

    private final int arc;
    private final Color borderColor;

    /**
     * @param layout (e.g. new BorderLayout(0, 12))
     * @param arc
     * @param borderColor
     */
    public RoundedPanel(LayoutManager layout, int arc, Color borderColor) {
        super(layout);
        this.arc = arc;
        this.borderColor = borderColor;
        setOpaque(false); // CRITICAL: parent background shows in corners
    }

    // Convenience constructor with default layout.
    public RoundedPanel(int arc, Color borderColor) {
        this(null, arc, borderColor);
    }

    // Paint rounded filled background
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
    }

    // Paint rounded 1 px border
    @Override
    protected void paintBorder(Graphics g) {
        if (borderColor == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        // Inset by 0.5 px so the stroke doesn't bleed outside the component bounds
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
    }

    // Let Swing knows its transparent so it paints parent background behind corners.
    @Override
    public boolean isOpaque() {
        return false;
    }
}
