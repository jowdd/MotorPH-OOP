package com.group.motorph.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.group.motorph.model.User;
import com.group.motorph.service.AuthenticationService;
import com.group.motorph.ui.components.UITheme;

/**
 * Login window for the MotorPH Payroll System.
 */
public class LoginFrame extends JFrame {

    // Field border colour
    private static final Color FIELD_BORDER = new Color(0xD9D9D9);

    private final HintTextField usernameField = new HintTextField("Username or Email", 18);
    private final HintPasswordField passwordField = new HintPasswordField("Password", 18);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final AuthenticationService authService = new AuthenticationService();

    public LoginFrame() {
        setTitle("MotorPH");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BACKGROUND);
        root.setBorder(new EmptyBorder(40, 40, 40, 40));

        RoundedPanel card = new RoundedPanel(28);
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(470, 390));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(34, 42, 30, 42));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 10, 0);

        JLabel title = new JLabel("MotorPH Payroll System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.PLAIN, 25));
        title.setForeground(UITheme.TEXT_DARK);
        g.gridy = 0;
        g.insets = new Insets(8, 0, 28, 0);
        content.add(title, g);

        JLabel subtitle = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(UITheme.TEXT_MUTED);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 14, 0);
        content.add(subtitle, g);

        usernameField.setPreferredSize(new Dimension(0, 40));
        styleField(usernameField);
        g.gridy = 2;
        g.insets = new Insets(0, 0, 10, 0);
        content.add(wrapFieldWithIcon(usernameField, "\uD83D\uDC64"), g);

        passwordField.setPreferredSize(new Dimension(0, 40));
        styleField(passwordField);
        g.gridy = 3;
        g.insets = new Insets(0, 0, 8, 0);
        content.add(wrapFieldWithIcon(passwordField, "\uD83D\uDD11"), g);

        JPanel linksPanel = new JPanel(new BorderLayout());
        linksPanel.setOpaque(false);

        JButton forgotBtn = linkButton("Forgot Password?");
        JButton registerBtn = linkButton("Register");

        linksPanel.add(forgotBtn, BorderLayout.WEST);
        linksPanel.add(registerBtn, BorderLayout.EAST);

        g.gridy = 4;
        g.insets = new Insets(0, 0, 12, 0);
        content.add(linksPanel, g);

        JButton loginBtn = new RoundedButton("Log In");
        loginBtn.setPreferredSize(new Dimension(0, 38));
        g.gridy = 5;
        g.insets = new Insets(0, 0, 12, 0);
        content.add(loginBtn, g);

        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        messageLabel.setForeground(UITheme.DANGER);
        g.gridy = 6;
        g.insets = new Insets(0, 0, 0, 0);
        content.add(messageLabel, g);

        card.add(content);
        root.add(card);

        setContentPane(root);

        loginBtn.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());

        forgotBtn.addActionListener(e -> showInfoScreen(
                "Please contact the IT Department to\nreset your password.",
                "Forgot Password"
        ));

        registerBtn.addActionListener(e -> showInfoScreen(
                "Please contact the IT Department to\ncreate an account.",
                "Register"
        ));
    }

    private void attemptLogin() {
        String username = usernameField.getActualText().trim();
        String password = new String(passwordField.getActualPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        User user = authService.login(username, password);
        if (user != null) {
            showSuccess("Login successful! Loading dashboard...");
            SwingUtilities.invokeLater(() -> {
                new MainFrame(user).setVisible(true);
                dispose();
            });
        } else {
            showError("Invalid username or password. Please try again.");
            passwordField.resetPlaceholder();
        }
    }

    private void showInfoScreen(String message, String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JLabel label = new JLabel(
                "<html><div style='text-align:center;'>" +
                        message.replace("\n", "<br>") +
                        "</div></html>",
                SwingConstants.CENTER
        );
        label.setFont(new Font("SansSerif", Font.PLAIN, 18));
        label.setForeground(new Color(0x2F80ED));
        label.setBorder(new EmptyBorder(20, 20, 20, 20));

        dialog.add(label);
        dialog.setVisible(true);
    }

    private void styleField(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setForeground(UITheme.TEXT_DARK);
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        field.setCaretColor(UITheme.TEXT_DARK);
    }

    private JPanel wrapFieldWithIcon(JComponent field, String iconText) {
        // Rounded wrapper that repaints its border on focus / hover
        JPanel wrapper = new JPanel(new BorderLayout()) {
            private Color currentBorder = FIELD_BORDER;
            {
                setOpaque(false);
                // Focus listener on the field drives the blue ring
                field.addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { currentBorder = new Color(0x2563EB); repaint(); }
                    @Override public void focusLost (FocusEvent e) { currentBorder = FIELD_BORDER; repaint(); }
                });

                // Hover listener on the wrapper
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                        if (!field.isFocusOwner()) { currentBorder = new Color(0x9CA3AF); repaint(); }
                    }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) {
                        if (!field.isFocusOwner()) { currentBorder = FIELD_BORDER; repaint(); }
                    }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }

            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentBorder);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };

        wrapper.setBorder(new EmptyBorder(0, 10, 0, 10));
        wrapper.setPreferredSize(new Dimension(0, 40));

        JLabel icon = new JLabel(iconText);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 14));
        icon.setForeground(new Color(0x8E8E8E));
        icon.setBorder(new EmptyBorder(0, 0, 0, 6));

        wrapper.add(icon, BorderLayout.WEST);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    @SuppressWarnings({"rawtypes"})
    private JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.PRIMARY);
        btn.setBorder(new EmptyBorder(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover: underline + darken
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                Map attrs = new HashMap(btn.getFont().getAttributes());
                attrs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                btn.setFont(btn.getFont().deriveFont(attrs));
                btn.setForeground(UITheme.PRIMARY_DARK);
            }
            @Override public void mouseExited(MouseEvent e) {
                Map attrs = new HashMap(btn.getFont().getAttributes());
                attrs.put(TextAttribute.UNDERLINE, -1);
                btn.setFont(btn.getFont().deriveFont(attrs));
                btn.setForeground(UITheme.PRIMARY);
            }
        });
        return btn;
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setForeground(UITheme.DANGER);
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setForeground(UITheme.SUCCESS);
    }

    // Rounded white card.
    private static class RoundedPanel extends JPanel {

        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(UITheme.PANEL_BG);
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g2.dispose();
        }
    }

    // Rounded blue button.
    private static class RoundedButton extends JButton {
        RoundedButton(String text) {
            super(text);
            setForeground(Color.WHITE);
            setFont(UITheme.FONT_BODY);
            setBorder(new EmptyBorder(10, 18, 10, 18));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = getModel().isPressed()
                    ? UITheme.PRIMARY_DARK.darker()
                    : (getModel().isRollover() ? UITheme.PRIMARY_DARK : UITheme.PRIMARY);

            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            g2.dispose();

            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // no border
        }
    }

    // Text field with placeholder support.
    private static class HintTextField extends JTextField {
        private boolean showingHint = true;

        HintTextField(String hint, int columns) {
            super(hint, columns);
            setForeground(new Color(0x9A9A9A));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showingHint) {
                        setText("");
                        setForeground(UITheme.TEXT_DARK);
                        showingHint = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().trim().isEmpty()) {
                        setText(hint);
                        setForeground(new Color(0x9A9A9A));
                        showingHint = true;
                    }
                }
            });
        }

        String getActualText() {
            return showingHint ? "" : getText();
        }
    }

    // Password field with placeholder support.
    private static class HintPasswordField extends JPasswordField {
        private final String hint;
        private boolean showingHint = true;
        private final char defaultEcho;
        private static final Color PLACEHOLDER_FG = new Color(0x9A9A9A);
        private static final Color CARET_HIDDEN    = new Color(0, 0, 0, 0);

        HintPasswordField(String hint, int columns) {
            super(columns);
            this.hint = hint;
            this.defaultEcho = getEchoChar();

            setText(hint);
            setForeground(PLACEHOLDER_FG);
            setEchoChar((char) 0);
            setCaretColor(CARET_HIDDEN); // no caret while placeholder is visible

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showingHint) {
                        activateRealInput();
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getPassword().length == 0) {
                        resetPlaceholder();
                    }
                }
            });

            // Handle the case where the field already has focus when the placeholder is being shown
            addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    if (showingHint && !Character.isISOControl(e.getKeyChar())) {
                        activateRealInput();
                    }
                }
            });

            // Handle a direct mouse click
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    if (showingHint) {
                        activateRealInput();
                    }
                }
            });
        }

        // Switch from placeholder state to real-input state.
        private void activateRealInput() {
            setText("");
            setForeground(UITheme.TEXT_DARK);
            setEchoChar(defaultEcho);
            setCaretColor(UITheme.TEXT_DARK);
            showingHint = false;
        }

        char[] getActualPassword() {
            return showingHint ? new char[0] : getPassword();
        }

        void resetPlaceholder() {
            setText(hint);
            setForeground(PLACEHOLDER_FG);
            setEchoChar((char) 0);
            setCaretColor(CARET_HIDDEN); // hide caret so "Password" has no cursor blinking in it
            showingHint = true;
        }
    }
}