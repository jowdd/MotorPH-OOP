package com.group.motorph.ui.finance;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.group.motorph.model.User;
import com.group.motorph.ui.components.UITheme;

/**
 * Finance panel router. Delegates to the appropriate sub-panel: "attendance" -
 * FinanceAttendancePanel "allpayslips" - FinancePayslipsPanel
 */
public class FinancePanel extends JPanel {

    public FinancePanel(User user, String view) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        if ("attendance".equals(view)) {
            add(new FinanceAttendancePanel(user), BorderLayout.CENTER);
        } else {
            add(new FinancePayslipsPanel(user), BorderLayout.CENTER);
        }
    }
}
