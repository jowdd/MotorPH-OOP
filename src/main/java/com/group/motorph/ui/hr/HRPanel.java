package com.group.motorph.ui.hr;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.group.motorph.model.User;
import com.group.motorph.ui.components.UITheme;

/**
 * HR panel router. Delegates to the appropriate sub-panel:
 *   "employees" -> HREmployeePanel
 *   "leaves"    -> HRLeavePanel
 */
public class HRPanel extends JPanel {

    public HRPanel(User user, String view) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        if ("employees".equals(view)) {
            add(new HREmployeePanel(user), BorderLayout.CENTER);
        } else {
            add(new HRLeavePanel(user), BorderLayout.CENTER);
        }
    }
}
