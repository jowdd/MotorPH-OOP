package com.group.motorph.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/**
 * Rounded-corner button. Replaces the default Swing rectangular JButton
 */
public class RoundedButton extends JButton {

    private static final int ARC = 10;

    private final Color normalBg;
    private final Color hoverBg;
    private boolean hovered = false;

    /**
     * @param text Button label
     * @param bg Normal background color
     * @param hoverBg Background color when mouse is over the button
     * @param fg Foreground / text color
     */
    public RoundedButton(String text, Color bg, Color hoverBg, Color fg) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = hoverBg;

        // Let paintComponent handle all background rendering
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        setForeground(fg);
        setFont(UITheme.FONT_BODY);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 14, 8, 14));

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
        g2.setColor(hovered ? hoverBg : normalBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
        g2.dispose();
        // Let Swing render the text and icon on top
        super.paintComponent(g);
    }

    // Required so Swing knows the background is not a solid filled rectangle.
    @Override
    public boolean isOpaque() {
        return false;
    }
}
