/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.group.motorph;

import com.group.motorph.ui.LoginFrame;
import com.group.motorph.ui.UITheme;
import com.group.motorph.util.ResourceSeeder;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UITheme.setAppLookAndFeel();
        // Ensure runtime writable data files exist before launching the UI.
        ResourceSeeder.seedOnce();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}