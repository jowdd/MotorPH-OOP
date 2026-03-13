package com.group.motorph.ui.it;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.group.motorph.model.User;
import com.group.motorph.service.EmployeeService;
import com.group.motorph.service.UserService;
import com.group.motorph.ui.components.RoundedPanel;
import com.group.motorph.ui.components.RoundedSearchField;
import com.group.motorph.ui.components.UITheme;

/**
 * IT panel – User Management (add / edit / delete system users).
 */
public class ITPanel extends JPanel {

    private final User currentUser;
    private final UserService userService = new UserService();
    private final EmployeeService employeeService = new EmployeeService();

    private DefaultTableModel tableModel;
    private JTable userTable;
    private List<User> userList;
    private List<User> allUsers;
    private JButton editBtn;
    private JButton deleteBtn;
    private RoundedSearchField searchField;

    public ITPanel(User currentUser, String view) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        buildUserManagement();
    }

    //  PAGE: USER MANAGEMENT
    private void buildUserManagement() {
        JButton addBtn = UITheme.successButton("Add User");
        addBtn.addActionListener(e -> openUserDialog(null));

        editBtn = UITheme.primaryButton("Edit");
        deleteBtn = UITheme.dangerButton("Delete");
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        editBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0 && userList != null && row < userList.size()) {
                openUserDialog(userList.get(row));
            }
        });
        deleteBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0 && userList != null && row < userList.size()) {
                confirmDeleteUser(row);
            }
        });

        // Action Buttons
        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBar.setBackground(UITheme.PANEL_BG);
        leftBar.add(addBtn);
        leftBar.add(editBtn);
        leftBar.add(deleteBtn);

        // Search bar
        searchField = new RoundedSearchField("Search by Employee ID or Name");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterUsers();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterUsers();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterUsers();
            }
        });

        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(UITheme.PANEL_BG);
        topBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(searchField, BorderLayout.CENTER);

        // Columns: Employee ID | Employee Name | Username | Role
        tableModel = new DefaultTableModel(
                new String[]{"Employee ID", "Employee Name", "Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        UITheme.styleTable(userTable);
        userTable.setRowHeight(38);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        userTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = userTable.getSelectedRow() >= 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
        });

        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 12), 12, UITheme.PANEL_BORDER);
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(topBar, BorderLayout.NORTH);
        card.add(UITheme.scrollPane(userTable), BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BACKGROUND);
        wrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        wrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("User Management"), BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
        refreshUsers();
    }

    private void refreshUsers() {
        allUsers = userService.getAllUsers();
        allUsers.sort(Comparator.comparingInt(u -> {
            try {
                return Integer.valueOf(u.getEmployeeId() != null ? u.getEmployeeId().trim() : "");
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        }));
        filterUsers();
    }

    /**
     * Filters the user table by employee ID or employee name.
     *
     * Display names are not stored in {@code users.csv}, so the method resolves
     * each row's employee ID back through {@code EmployeeService} before it can
     * support name-based search and rendering.
     */
    private void filterUsers() {
        tableModel.setRowCount(0);
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        userList = (allUsers == null) ? new java.util.ArrayList<>() : allUsers;
        if (!q.isEmpty()) {
            userList = userList.stream()
                    .filter(u -> {
                        String empId = u.getEmployeeId() != null ? u.getEmployeeId().trim().toLowerCase() : "";
                        String empIdRaw = u.getEmployeeId() != null ? u.getEmployeeId().trim() : "";
                        com.group.motorph.model.Employee emp = employeeService.getEmployeeById(empIdRaw);
                        String empName = emp != null ? emp.getFullName().toLowerCase() : "";
                        return empId.contains(q) || empName.contains(q);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        for (User u : userList) {
            String empId = u.getEmployeeId() != null ? u.getEmployeeId().trim() : "";
            com.group.motorph.model.Employee emp = employeeService.getEmployeeById(empId);
            String empName = emp != null ? emp.getFullName() : "—";
            tableModel.addRow(new Object[]{
                empId,
                empName,
                u.getUsername() != null ? u.getUsername() : "",
                u.getRole() != null ? u.getRole() : ""
            });
        }
        if (editBtn != null) {
            editBtn.setEnabled(false);
        }
        if (deleteBtn != null) {
            deleteBtn.setEnabled(false);
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
        if (!yes) {
            return;
        }
        boolean ok = userService.deleteUser(u.getUsername());
        if (ok) {
            refreshUsers(); 
        }else {
            JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens the add/edit user dialog.
     *
     * For new accounts the form only offers employee IDs that do not already
     * have a linked user account. For existing accounts the username stays
     * locked, and supplying a new password is optional because the service will
     * only re-hash and overwrite it when a non-blank value is provided.
     */
    private void openUserDialog(User existing) {
        boolean isEdit = (existing != null);
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, isEdit ? "Edit User" : "Add User",
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
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(7, 4, 7, 4);
        g.weightx = 1.0;

        JTextField usernameFld = new JTextField((isEdit && existing != null) ? existing.getUsername() : "", 18);
        JPasswordField passFld = new JPasswordField("", 18); // never pre-fill with stored hash
        usernameFld.setFont(UITheme.FONT_BODY);
        passFld.setFont(UITheme.FONT_BODY);

        // Employee ID dropdown for Add User – only unassigned employee IDs
        JComboBox<String> empIdCombo = new JComboBox<>();
        empIdCombo.setFont(UITheme.FONT_BODY);
        if (!isEdit) {
            Set<String> usedIds = new HashSet<>();
            for (User u : userService.getAllUsers()) {
                if (u.getEmployeeId() != null) {
                    usedIds.add(u.getEmployeeId().trim());
                }
            }
            for (var emp : employeeService.getAllEmployees()) {
                String eid = emp.getEmployeeId();
                if (eid != null && !usedIds.contains(eid.trim())) {
                    empIdCombo.addItem(eid.trim());
                }
            }
        }

        String[] roles = {"HR", "Finance", "IT", "Employee", "Admin"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(UITheme.FONT_BODY);
        if ((isEdit && existing != null) && existing.getRole() != null) {
            for (String r : roles) {
                if (r.equalsIgnoreCase(existing.getRole())) {
                    roleCombo.setSelectedItem(r);
                    break;
                }
            }
        }

        if (isEdit) {
            usernameFld.setEditable(false);
        }

        JLabel msgLbl = new JLabel(" ");
        msgLbl.setFont(UITheme.FONT_SMALL);

        int row = 0;
        addRow(form, g, row++, "Username:", usernameFld);
        addRow(form, g, row++, isEdit ? "New Password" + ":" : "Password:", passFld);
        if (!isEdit) {
            addRow(form, g, row++, "Employee ID:", empIdCombo);
        }
        addRow(form, g, row++, isEdit ? "New Role:" : "Role:", roleCombo);
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        form.add(msgLbl, g);

        dlg.add(form, BorderLayout.CENTER);

        // Buttons
        JButton saveBtn = UITheme.primaryButton(isEdit ? "Save Changes" : "Add User");
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String username = usernameFld.getText().trim();
            String password = new String(passFld.getPassword()).trim();
            String role = ((String) roleCombo.getSelectedItem()).toUpperCase();
            String empId = (isEdit && existing != null) ? existing.getEmployeeId()
                    : (empIdCombo.getSelectedItem() != null
                    ? (String) empIdCombo.getSelectedItem() : "");

            if (username.isEmpty()) {
                showMsg(msgLbl, "Username is required.", UITheme.DANGER);
                return;
            }
            if (!isEdit && password.isEmpty()) {
                showMsg(msgLbl, "Password is required.", UITheme.DANGER);
                return;
            }
            if (!isEdit && empId.isEmpty()) {
                showMsg(msgLbl, "No available employees to assign.", UITheme.DANGER);
                return;
            }

            String confirmMsg = isEdit
                    ? "Are you sure you want to save changes?"
                    : "Are you sure you want to add this user?";
            if (!showConfirmDialog(dlg, confirmMsg)) {
                return;
            }

            boolean ok = isEdit
                    ? userService.updateUser(username, password, empId, role)
                    : userService.createUser(username, password, empId, role);

            if (ok) {
                dlg.dispose();
                refreshUsers();
            } else {
                showMsg(msgLbl,
                        isEdit ? "Update failed." : "Failed — username may already exist.",
                        UITheme.DANGER);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnRow.setBackground(UITheme.PANEL_BG);
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    //  Confirmation dialog
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

    // Helpers
    private static void addRow(JPanel form, GridBagConstraints g, int row,
            String label, JComponent field) {
        g.gridwidth = 1;
        g.gridx = 0;
        g.gridy = row;
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

}
