package com.group.motorph.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.group.motorph.model.User;
import com.group.motorph.ui.components.DialogUtil;
import com.group.motorph.ui.components.UITheme;
import com.group.motorph.ui.employee.EmployeeDailyTimeRecordPanel;
import com.group.motorph.ui.employee.EmployeeLeaveApplicationPanel;
import com.group.motorph.ui.employee.EmployeePayslipsPanel;
import com.group.motorph.ui.finance.FinancePanel;
import com.group.motorph.ui.hr.HRPanel;
import com.group.motorph.ui.it.ITPanel;

public class MainFrame extends JFrame {

    private final User user;
    private final JPanel contentArea = new JPanel(new BorderLayout());
    private final List<JButton> navButtons = new ArrayList<>();

    public MainFrame(User user) {
        this.user = user;
        setTitle("MotorPH");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        //Top header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.SIDEBAR_BORDER),
                new EmptyBorder(0, 20, 0, 20)));

        JLabel brandLbl = new JLabel("MotorPH Payroll System");
        brandLbl.setFont(UITheme.FONT_BRAND);
        brandLbl.setForeground(UITheme.TEXT_DARK);
        header.add(brandLbl, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Body panel, placed below the header
        JPanel body = new JPanel(new BorderLayout());
        body.add(buildSidebar(), BorderLayout.WEST);

        contentArea.setBackground(UITheme.BACKGROUND);
        contentArea.setBorder(new EmptyBorder(0, 0, 0, 0));
        body.add(contentArea, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // Show Daily Time Record as the default landing page
        show(new EmployeeDailyTimeRecordPanel(user));
    }

    /**
     * Builds the left navigation based on the logged-in user's role.
     *
     * Admin is treated as a superset role, so the boolean flags below allow the
     * same routing code to reuse employee/HR/finance/IT modules while still
     * exposing the admin-only combination of all pages.
     */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(195, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.SIDEBAR_BORDER));

        sidebar.add(Box.createVerticalStrut(10));

        String role = user.getRole().toUpperCase();

        boolean isAdmin = "ADMIN".equals(role);
        boolean isHR = "HR".equals(role) || isAdmin;
        boolean isFinance = "FINANCE".equals(role) || isAdmin;
        boolean isIT = "IT".equals(role) || isAdmin;
        boolean isEmployee = "EMPLOYEE".equals(role) || isAdmin;

        if (isHR && !isFinance) {
            sidebar.add(navBtn("Daily Time Record", () -> show(new EmployeeDailyTimeRecordPanel(user))));
            sidebar.add(navBtn("Leave Application", () -> show(new EmployeeLeaveApplicationPanel(user))));
            sidebar.add(navBtn("My Payslip", () -> show(new EmployeePayslipsPanel(user))));
            sidebar.add(navBtn("Employee Management", () -> show(new HRPanel(user, "employees"))));
            sidebar.add(navBtn("Leave Management", () -> show(new HRPanel(user, "leaves"))));
        }

        if (isFinance && !isAdmin) {
            // Finance sidebar order: DTR, Leave Application, My Payslip, Payroll Management, Employees Payslips
            sidebar.add(navBtn("Daily Time Record", () -> show(new EmployeeDailyTimeRecordPanel(user))));
            sidebar.add(navBtn("Leave Application", () -> show(new EmployeeLeaveApplicationPanel(user))));
            sidebar.add(navBtn("My Payslip", () -> show(new EmployeePayslipsPanel(user))));
            sidebar.add(navBtn("Payroll Management", () -> show(new FinancePanel(user, "attendance"))));
            sidebar.add(navBtn("Employees Payslips", () -> show(new FinancePanel(user, "allpayslips"))));
        }

        if (isAdmin) {
            // Admin sees full access in the requested order
            sidebar.add(navBtn("Daily Time Record", () -> show(new EmployeeDailyTimeRecordPanel(user))));
            sidebar.add(navBtn("Leave Application", () -> show(new EmployeeLeaveApplicationPanel(user))));
            sidebar.add(navBtn("My Payslip", () -> show(new EmployeePayslipsPanel(user))));
            sidebar.add(navBtn("Payroll Management", () -> show(new FinancePanel(user, "attendance"))));
            sidebar.add(navBtn("Employees Payslips", () -> show(new FinancePanel(user, "allpayslips"))));
            sidebar.add(navBtn("Employee Management", () -> show(new HRPanel(user, "employees"))));
            sidebar.add(navBtn("Leave Management", () -> show(new HRPanel(user, "leaves"))));
            sidebar.add(navBtn("User Management", () -> show(new ITPanel(user, "users"))));
        }

        if (isIT && !isAdmin) {
            sidebar.add(navBtn("Daily Time Record", () -> show(new EmployeeDailyTimeRecordPanel(user))));
            sidebar.add(navBtn("Leave Application", () -> show(new EmployeeLeaveApplicationPanel(user))));
            sidebar.add(navBtn("My Payslip", () -> show(new EmployeePayslipsPanel(user))));
            sidebar.add(navBtn("User Management", () -> show(new ITPanel(user, "users"))));
        }

        if (isEmployee && !isHR && !isFinance && !isIT && !isAdmin) {
            sidebar.add(navBtn("Daily Time Record", () -> show(new EmployeeDailyTimeRecordPanel(user))));
            sidebar.add(navBtn("Leave Application", () -> show(new EmployeeLeaveApplicationPanel(user))));
            sidebar.add(navBtn("My Payslip", () -> show(new EmployeePayslipsPanel(user))));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = UITheme.navButton("Log Out");
        logoutBtn.addActionListener(e -> logout());
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(12));

        return sidebar;
    }

    private JButton navBtn(String label, Runnable action) {
        JButton btn = UITheme.navButton(label);
        navButtons.add(btn);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            setActiveButton(btn);
            action.run();
        });
        return btn;
    }

    private void setActiveButton(JButton active) {
        for (JButton b : navButtons) {
            UITheme.setNavActive(b, b == active);
        }
    }

    private void show(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void logout() {
        if (DialogUtil.showConfirmDialog(this, "Are you sure you want to log out?")) {
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
        }
    }
}
