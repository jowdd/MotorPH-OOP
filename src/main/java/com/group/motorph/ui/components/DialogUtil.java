package com.group.motorph.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

// Shared modal dialogs
public class DialogUtil {

    private DialogUtil() {
    }

    // Shows a Yes/No confirmation. Returns true if the user clicked Yes.
    public static boolean showConfirmDialog(Window parent, String message) {
        JDialog dlg = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 200);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JLabel lbl = new JLabel(
                "<html><div style='text-align:center;'>" + message + "</div></html>",
                SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setForeground(UITheme.PRIMARY);
        lbl.setBorder(new EmptyBorder(30, 20, 10, 20));
        dlg.add(lbl, BorderLayout.CENTER);

        final boolean[] result = {false};
        JButton yesBtn = UITheme.primaryButton("Yes");
        JButton noBtn = UITheme.dangerButton("No");
        yesBtn.setPreferredSize(new Dimension(100, 36));
        noBtn.setPreferredSize(new Dimension(100, 36));
        yesBtn.addActionListener(e -> {
            result[0] = true;
            dlg.dispose();
        });
        noBtn.addActionListener(e -> {
            result[0] = false;
            dlg.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(noBtn);
        btnRow.add(yesBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
        return result[0];
    }

    // Shows an informational dialog with a single OK button.
    public static void showInfoDialog(Window parent, String message) {
        JDialog dlg = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 200);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JLabel lbl = new JLabel(
                "<html><div style='text-align:center;'>" + message + "</div></html>",
                SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setBorder(new EmptyBorder(30, 20, 10, 20));
        dlg.add(lbl, BorderLayout.CENTER);

        JButton okBtn = UITheme.primaryButton("OK");
        okBtn.setPreferredSize(new Dimension(100, 36));
        okBtn.addActionListener(e -> dlg.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(okBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
