package com.group.motorph.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollRecord;

/**
 * Payslip detail dialog - opened from both the Finance payslips panel and the
 * employee's own payslips panel.
 */
public class PayslipDialog {

    private PayslipDialog() {
    }

    // Opens the payslip detail dialog for the given record and employee.
    public static void show(PayrollRecord rec, Employee emp) {
        JDialog dlg = new JDialog();
        dlg.setTitle("Employee Payslip");
        dlg.setSize(600, 580);
        dlg.setLocationRelativeTo(null);
        dlg.setModal(true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel titleLbl = new JLabel("Employee Payslip");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.SUCCESS);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(8));

        String monthName = (rec.getMonth() >= 1 && rec.getMonth() <= 12)
                ? Month.of(rec.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) : "";

        addInfoRow(content, "Payslip ID:", UITheme.PRIMARY,
                rec.getPayslipId() != null ? rec.getPayslipId() : "");
        addInfoPair(content, "Month:", monthName, "Year:", String.valueOf(rec.getYear()));
        if (emp != null) {
            addInfoRow(content, "Employee ID:", UITheme.PRIMARY, emp.getEmployeeId());
            addInfoPair(content, "Name:", emp.getFullName(),
                    "Position:", emp.getPosition() != null ? emp.getPosition() : "");
        }
        content.add(Box.createVerticalStrut(16));

        content.add(sectionBanner("Deductions"));
        content.add(Box.createVerticalStrut(8));
        addDetailRow(content, "SSS:", String.format("\u20b1%,.2f", rec.getSss()));
        addDetailRow(content, "PhilHealth:", String.format("\u20b1%,.2f", rec.getPhilHealth()));
        addDetailRow(content, "PAG-IBIG:", String.format("\u20b1%,.2f", rec.getPagIbig()));
        addDetailRow(content, "Withholding Tax:", String.format("\u20b1%,.2f", rec.getWithholdingTax()));
        addDetailRow(content, "Total Deductions:", String.format("\u20b1%,.2f", rec.getTotalDeductions()));
        content.add(Box.createVerticalStrut(16));

        content.add(sectionBanner("Summary"));
        content.add(Box.createVerticalStrut(8));
        addDetailRow(content, "Hours Worked:", String.format("%.2f hrs", rec.getHoursWorked()));
        addDetailRow(content, "Overtime Hours:", String.format("%.2f hrs", rec.getOvertimeHours()));
        addDetailRow(content, "Gross Pay:", String.format("\u20b1%,.2f", rec.getGrossPay()));
        addDetailRow(content, "Net Pay:", String.format("\u20b1%,.2f", rec.getNetPay()));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        south.setBackground(UITheme.PANEL_BG);
        JButton downloadBtn = UITheme.primaryButton("Download PDF");
        JButton closeBtn = UITheme.dangerButton("Close");
        downloadBtn.addActionListener(e
                -> DialogUtil.showInfoDialog(dlg, "PDF export is not yet available in this version."));
        closeBtn.addActionListener(e -> dlg.dispose());
        south.add(closeBtn);
        south.add(downloadBtn);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // Private helpers
    private static JPanel sectionBanner(String text) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(0x2F3142));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(UITheme.FONT_HEADER);
        lbl.setForeground(Color.WHITE);
        banner.add(lbl, BorderLayout.WEST);
        return banner;
    }

    private static void addInfoRow(JPanel panel, String label, Color valueColor, String value) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(1, 0, 1, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridy = 0;
        gc.gridx = 0;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY);
        l.setPreferredSize(new Dimension(110, 20));
        row.add(l, gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_BODY);
        v.setForeground(valueColor);
        row.add(v, gc);
        panel.add(row);
    }

    private static void addInfoPair(JPanel panel,
            String lbl1, String val1, String lbl2, String val2) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(1, 0, 1, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridy = 0;
        gc.gridx = 0;
        JLabel l1 = new JLabel(lbl1);
        l1.setFont(UITheme.FONT_BODY);
        l1.setPreferredSize(new Dimension(110, 20));
        row.add(l1, gc);
        gc.gridx = 1;
        JLabel v1 = new JLabel(val1);
        v1.setFont(UITheme.FONT_BODY);
        v1.setForeground(UITheme.PRIMARY);
        v1.setPreferredSize(new Dimension(170, 20));
        row.add(v1, gc);
        gc.gridx = 2;
        gc.insets = new Insets(1, 16, 1, 6);
        JLabel l2 = new JLabel(lbl2);
        l2.setFont(UITheme.FONT_BODY);
        l2.setPreferredSize(new Dimension(80, 20));
        row.add(l2, gc);
        gc.gridx = 3;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(1, 0, 1, 0);
        JLabel v2 = new JLabel(val2);
        v2.setFont(UITheme.FONT_BODY);
        v2.setForeground(UITheme.PRIMARY);
        row.add(v2, gc);
        panel.add(row);
    }

    static void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY);
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_BODY);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(2));
    }
}
