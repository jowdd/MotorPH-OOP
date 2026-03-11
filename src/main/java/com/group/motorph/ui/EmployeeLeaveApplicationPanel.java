package com.group.motorph.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.group.motorph.model.LeaveRequest;
import com.group.motorph.model.User;
import com.group.motorph.service.LeaveService;

/**
 * Leave Application panel – view own leave requests and submit new ones.
 * Columns: Type of Request | Leave Start Date | Leave End Date | Remarks (colour-coded).
 * Matches the IT / Employee design reference exactly.
 */
public class EmployeeLeaveApplicationPanel extends JPanel {

    // Dates shown as MM/dd/yyyy in table (matching the design reference screenshots)
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);
    private static final Color CYAN_DATE           = new Color(0x0099CC);
    private static final Color ORANGE              = new Color(0xFF8C00);

    private final User         currentUser;
    private final LeaveService leaveService = new LeaveService();

    private List<LeaveRequest> allLeaves;
    private DefaultTableModel  tableModel;

    public EmployeeLeaveApplicationPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        build();
    }

    // ─────────────────────────────────────────────────────────────────
    private void build() {
        String empId = currentUser.getEmployeeId() != null ? currentUser.getEmployeeId().trim() : "";
        allLeaves = leaveService.getLeaveRequestsByEmployee(empId);

        // ── "Request for Leave" button – top-left of card  (design reference)
        JButton requestBtn = UITheme.primaryButton("Request for Leave");
        requestBtn.addActionListener(e -> openRequestDialog());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setBackground(UITheme.PANEL_BG);
        topBar.add(requestBtn);

        // ── Table  (columns: Type | Start | End | Remarks)
        tableModel = new DefaultTableModel(
                new String[]{"Type of Request", "Leave Start Date", "Leave End Date", "Remarks"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(36);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        // Teal date columns (matching design)
        DefaultTableCellRenderer teal = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setForeground(CYAN_DATE);
                return this;
            }
        };
        table.getColumnModel().getColumn(1).setCellRenderer(teal);
        table.getColumnModel().getColumn(2).setCellRenderer(teal);

        // Color-coded Remarks column
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, sel, foc, row, col);
                String v = value != null ? value.toString() : "";
                if      ("Approved".equalsIgnoreCase(v)) setForeground(UITheme.SUCCESS);
                else if ("Declined".equalsIgnoreCase(v)) setForeground(UITheme.DANGER);
                else                                      setForeground(ORANGE);
                setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
                return this;
            }
        });

        // ── Card assembly
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PANEL_BORDER),
                new EmptyBorder(16, 16, 16, 16)));
        card.add(topBar,                    BorderLayout.NORTH);
        card.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(UITheme.BACKGROUND);
        cardWrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        cardWrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("Leave Application"), BorderLayout.NORTH);
        add(cardWrap, BorderLayout.CENTER);

        refreshTable();
    }

    // ─────────────────────────────────────────────────────────────────
    private void refreshTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (LeaveRequest lr : allLeaves) {
            String startStr = lr.getStartDate() != null ? lr.getStartDate().format(DATE_FMT) : "";
            String endStr   = lr.getEndDate()   != null ? lr.getEndDate()  .format(DATE_FMT) : "";
            String remarks  = lr.getStatus()    != null ? lr.getStatus()                      : "Pending";
            tableModel.addRow(new Object[]{ lr.getLeaveType(), startStr, endStr, remarks });
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Request for Leave Dialog  (matches design screenshot)
    //  Fields: Leave Start Date → Leave End Date → Type of Leave
    // ─────────────────────────────────────────────────────────────────

    private void openRequestDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, "Request for Leave",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(400, 330);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        // Dialog title  (large, PRIMARY blue – matching design)
        JLabel titleLbl = new JLabel("Request for Leave");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.PRIMARY);
        titleLbl.setBorder(new EmptyBorder(20, 24, 8, 24));
        dlg.add(titleLbl, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.PANEL_BG);
        form.setBorder(new EmptyBorder(4, 24, 8, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(8, 4, 8, 4);
        g.weightx = 1.0;

        final LocalDate[] startDate = {LocalDate.now()};
        final LocalDate[] endDate   = {LocalDate.now()};

        JButton startBtn = calBtn(startDate[0]);
        JButton endBtn   = calBtn(endDate[0]);

        startBtn.addActionListener(e -> {
            LocalDate d = DatePickerDialog.show(dlg, "Select Leave Start Date", startDate[0]);
            if (d != null) { startDate[0] = d; startBtn.setText(formatDate(d)); }
        });
        endBtn.addActionListener(e -> {
            LocalDate d = DatePickerDialog.show(dlg, "Select Leave End Date", endDate[0]);
            if (d != null) { endDate[0] = d; endBtn.setText(formatDate(d)); }
        });

        String[] types = {"Vacation Leave", "Sick Leave", "Emergency Leave",
                          "Maternity Leave", "Paternity Leave", "Others"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setFont(UITheme.FONT_BODY);

        JLabel msgLabel = new JLabel(" ");
        msgLabel.setFont(UITheme.FONT_SMALL);

        // Field order from design: Leave Start Date, Leave End Date, Type of Leave
        addFormRow(form, g, 0, "Leave Start Date:", startBtn);
        addFormRow(form, g, 1, "Leave End Date:",   endBtn);
        addFormRow(form, g, 2, "Type of Leave:",    typeCombo);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        form.add(msgLabel, g);

        dlg.add(form, BorderLayout.CENTER);

        // Buttons: Submit (blue), Cancel (red)
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnRow.setBackground(UITheme.PANEL_BG);
        JButton submitBtn = UITheme.primaryButton("Submit");
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        btnRow.add(submitBtn);
        btnRow.add(cancelBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dlg.dispose());
        submitBtn.addActionListener(e -> {
            LocalDate s  = startDate[0];
            LocalDate en = endDate[0];
            if (s.isAfter(en)) {
                msgLabel.setText("Start date must be on or before end date.");
                msgLabel.setForeground(UITheme.DANGER);
                return;
            }
            String type = (String) typeCombo.getSelectedItem();
            boolean ok = leaveService.submitLeaveRequest(
                    currentUser.getEmployeeId().trim(), s, en, type, "");
            if (ok) {
                allLeaves = leaveService.getLeaveRequestsByEmployee(
                        currentUser.getEmployeeId().trim());
                dlg.dispose();
                refreshTable();
            } else {
                msgLabel.setText("Failed to submit leave request. Please try again.");
                msgLabel.setForeground(UITheme.DANGER);
            }
        });

        dlg.setVisible(true);
    }

    // ── Helpers ────────────────────────────────────────────────────────
    private static JButton calBtn(LocalDate initial) {
        JButton btn = new JButton("\uD83D\uDCC5 " + formatDate(initial));
        btn.setFont(UITheme.FONT_BODY);
        btn.setBackground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static String formatDate(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));
    }

    private static void addFormRow(JPanel form, GridBagConstraints g,
                                   int row, String label, JComponent field) {
        g.gridwidth = 1; g.gridx = 0; g.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY);
        l.setPreferredSize(new Dimension(140, 24));
        form.add(l, g);
        g.gridx = 1;
        form.add(field, g);
    }
}
