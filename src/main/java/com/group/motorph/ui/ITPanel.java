package com.group.motorph.ui;

import com.group.motorph.model.Employee;
import com.group.motorph.model.User;
import com.group.motorph.service.EmployeeService;
import com.group.motorph.service.UserService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * IT panel – User Management (add / edit / delete system users).
 * Instantiated by MainFrame with view = "users".
 * Pages: DTR, Leave Application, Payslips are delegated to the shared employee panels.
 */
public class ITPanel extends JPanel {

    // Table action column indices
    private static final int EDIT_COL   = 3;
    private static final int DELETE_COL = 4;

    private final User            currentUser;
    private final UserService     userService     = new UserService();
    private final EmployeeService employeeService = new EmployeeService();

    private DefaultTableModel tableModel;
    private JTable            userTable;
    private List<User>        userList;

    public ITPanel(User currentUser, String view) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        buildUserManagement();
    }

    // ================================================================
    //  PAGE: USER MANAGEMENT
    // ================================================================

    private void buildUserManagement() {
        JButton addBtn = UITheme.primaryButton("Add User");
        addBtn.addActionListener(e -> openUserDialog(null));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setBackground(UITheme.PANEL_BG);
        topBar.add(addBtn);

        // Columns: Employee ID | Username | Role | Edit | Delete
        tableModel = new DefaultTableModel(
                new String[]{"Employee ID", "Username", "Role", "Edit", "Delete"}, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == EDIT_COL || c == DELETE_COL;
            }
        };
        userTable = new JTable(tableModel);
        UITheme.styleTable(userTable);
        userTable.setRowHeight(38);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        userTable.getColumnModel().getColumn(EDIT_COL)  .setPreferredWidth(80);
        userTable.getColumnModel().getColumn(DELETE_COL).setPreferredWidth(80);

        userTable.getColumnModel().getColumn(EDIT_COL)  .setCellRenderer(new BtnRenderer(UITheme.primaryButton("Edit")));
        userTable.getColumnModel().getColumn(DELETE_COL).setCellRenderer(new BtnRenderer(UITheme.dangerButton("Delete")));
        userTable.getColumnModel().getColumn(EDIT_COL)  .setCellEditor(new BtnEditor(UITheme.primaryButton("Edit")));
        userTable.getColumnModel().getColumn(DELETE_COL).setCellEditor(new BtnEditor(UITheme.dangerButton("Delete")));

        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = userTable.columnAtPoint(e.getPoint());
                int row = userTable.rowAtPoint(e.getPoint());
                if (row < 0 || userList == null || row >= userList.size()) return;
                if (col == EDIT_COL)   openUserDialog(userList.get(row));
                if (col == DELETE_COL) confirmDeleteUser(row);
            }
        });

        // Note label at bottom  (red, like the reference design)
        JLabel noteLabel = new JLabel(
                "NOTE: In this table, it should shows all the users of motorph payroll system");
        noteLabel.setFont(UITheme.FONT_SMALL);
        noteLabel.setForeground(UITheme.DANGER);
        noteLabel.setBorder(new EmptyBorder(10, 4, 4, 4));

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PANEL_BORDER),
                new EmptyBorder(16, 16, 16, 16)));
        card.add(topBar,                        BorderLayout.NORTH);
        card.add(UITheme.scrollPane(userTable), BorderLayout.CENTER);
        card.add(noteLabel,                     BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BACKGROUND);
        wrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        wrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("User Management"), BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
        refreshUsers();
    }

    private void refreshUsers() {
        tableModel.setRowCount(0);
        userList = userService.getAllUsers();
        for (User u : userList) {
            String empId = u.getEmployeeId() != null ? u.getEmployeeId().trim() : "";
            tableModel.addRow(new Object[]{
                empId,
                u.getUsername() != null ? u.getUsername() : "",
                u.getRole()     != null ? u.getRole()     : "",
                "Edit", "Delete"
            });
        }
    }

    private void confirmDeleteUser(int row) {
        User u = userList.get(row);
        if (u.getUsername().equals(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean yes = showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                "Are you sure you want to delete this user?");
        if (!yes) return;
        boolean ok = userService.deleteUser(u.getUsername());
        if (ok) refreshUsers();
        else JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================
    //  Add / Edit User Dialog
    // ================================================================

    private void openUserDialog(User existing) {
        boolean isEdit = (existing != null);
        Window parent  = SwingUtilities.getWindowAncestor(this);
        JDialog dlg    = new JDialog(parent, isEdit ? "Edit User" : "Add User",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, isEdit ? 320 : 360);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        // Dialog title
        JLabel titleLbl = new JLabel(isEdit ? "Edit User" : "Add User");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.PRIMARY);
        titleLbl.setBorder(new EmptyBorder(20, 24, 10, 24));
        dlg.add(titleLbl, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.PANEL_BG);
        form.setBorder(new EmptyBorder(4, 24, 8, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(7, 4, 7, 4);
        g.weightx = 1.0;

        JTextField  usernameFld = new JTextField(isEdit ? existing.getUsername()   : "", 18);
        JPasswordField passFld  = new JPasswordField(isEdit ? existing.getPassword() : "", 18);
        usernameFld.setFont(UITheme.FONT_BODY);
        passFld    .setFont(UITheme.FONT_BODY);

        // Employee ID only for Add User
        JTextField empIdFld = new JTextField(18);
        empIdFld.setFont(UITheme.FONT_BODY);

        String[] roles = {"HR", "Finance", "IT", "Employee", "Admin"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(UITheme.FONT_BODY);
        if (isEdit && existing.getRole() != null) {
            for (String r : roles) {
                if (r.equalsIgnoreCase(existing.getRole())) {
                    roleCombo.setSelectedItem(r);
                    break;
                }
            }
        }

        if (isEdit) usernameFld.setEditable(false);

        JLabel msgLbl = new JLabel(" ");
        msgLbl.setFont(UITheme.FONT_SMALL);

        int row = 0;
        addRow(form, g, row++, "Username:",    usernameFld);
        addRow(form, g, row++, isEdit ? "New Password:" : "Password:", passFld);
        if (!isEdit) addRow(form, g, row++, "Employee ID:", empIdFld);
        addRow(form, g, row++, isEdit ? "New Role:" : "Role:", roleCombo);
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        form.add(msgLbl, g);

        dlg.add(form, BorderLayout.CENTER);

        // Buttons
        JButton saveBtn   = UITheme.primaryButton(isEdit ? "Save Changes" : "Add User");
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String username = usernameFld.getText().trim();
            String password = new String(passFld.getPassword()).trim();
            String role     = ((String) roleCombo.getSelectedItem()).toUpperCase();
            String empId    = isEdit ? existing.getEmployeeId() : empIdFld.getText().trim();

            if (username.isEmpty()) {
                showMsg(msgLbl, "Username is required.", UITheme.DANGER); return;
            }
            if (password.isEmpty()) {
                showMsg(msgLbl, "Password is required.", UITheme.DANGER); return;
            }
            if (!isEdit && empId.isEmpty()) {
                showMsg(msgLbl, "Employee ID is required.", UITheme.DANGER); return;
            }
            if (!isEdit && employeeService.getEmployeeById(empId) == null) {
                showMsg(msgLbl, "Employee ID '" + empId + "' not found.", UITheme.DANGER); return;
            }

            String confirmMsg = isEdit
                    ? "Are you sure you want to save changes?"
                    : "Are you sure you want to add this user?";
            if (!showConfirmDialog(dlg, confirmMsg)) return;

            boolean ok = isEdit
                    ? userService.updateUser(username, password, empId, role)
                    : userService.createUser(username, password, empId, role);

            if (ok) { dlg.dispose(); refreshUsers(); }
            else      showMsg(msgLbl,
                    isEdit ? "Update failed." : "Failed — username may already exist.",
                    UITheme.DANGER);
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnRow.setBackground(UITheme.PANEL_BG);
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ================================================================
    //  Shared: custom blue-Yes / red-No confirm dialog
    // ================================================================

    static boolean showConfirmDialog(Window parent, String message) {
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
        JButton noBtn  = UITheme.dangerButton("No");
        yesBtn.setPreferredSize(new Dimension(100, 36));
        noBtn .setPreferredSize(new Dimension(100, 36));
        yesBtn.addActionListener(e -> { result[0] = true;  dlg.dispose(); });
        noBtn .addActionListener(e -> { result[0] = false; dlg.dispose(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(yesBtn);
        btnRow.add(noBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
        return result[0];
    }

    // ================================================================
    //  Helpers
    // ================================================================

    private static void addRow(JPanel form, GridBagConstraints g, int row,
                               String label, JComponent field) {
        g.gridwidth = 1;
        g.gridx = 0; g.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setPreferredSize(new Dimension(130, 24));
        form.add(lbl, g);
        g.gridx = 1;
        form.add(field, g);
    }

    private static void showMsg(JLabel lbl, String msg, Color color) {
        lbl.setText(msg);
        lbl.setForeground(color);
    }

    // ================================================================
    //  Generic in-table button renderer / editor
    // ================================================================

    private static class BtnRenderer implements TableCellRenderer {
        private final JButton proto;
        BtnRenderer(JButton proto) { this.proto = proto; }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) { return proto; }
    }

    private static class BtnEditor extends DefaultCellEditor {
        private final JButton btn;
        BtnEditor(JButton src) {
            super(new JCheckBox());
            btn = src;
            btn.addActionListener(e -> fireEditingStopped());
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int r, int c) { return btn; }
        @Override public Object getCellEditorValue() { return btn.getText(); }
    }
}
