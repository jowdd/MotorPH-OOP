package com.group.motorph;

import javax.swing.SwingUtilities;

import com.group.motorph.ui.LoginFrame;
import com.group.motorph.ui.UITheme;

public class Main {
    public static void main(String[] args) {
        UITheme.setAppLookAndFeel();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}