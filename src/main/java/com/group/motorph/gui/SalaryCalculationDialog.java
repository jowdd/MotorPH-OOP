package com.group.motorph.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollCalculationService;
import com.group.motorph.model.PayrollSystem;
import com.group.motorph.model.SalaryCalculationResult;
import com.group.motorph.model.TimeLog;

/**
 * Dialog for calculating and displaying an employee's salary for a selected month and year.
 */
public class SalaryCalculationDialog extends JDialog {

    /** Payroll system reference for retrieving employee data and time logs */
    private final PayrollSystem payrollSystem;

    /** Employee for whom the salary will be calculated */
    private final Employee employee;

    /** Combo box for selecting the month of salary calculation */
    private final JComboBox<String> monthSelector;

    /** Combo box for selecting the year of salary calculation */
    private final JComboBox<Integer> yearSelector;

    /** Panel to render calculated salary metrics */
    private final JPanel resultsPanel;

    /** Formatter for displaying amounts in Philippine Peso */
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "PH"));

    /**
     * Constructs a SalaryCalculationDialog for a specific employee.
     *
     * @param owner        the parent JFrame for modal dialog positioning
     * @param payrollSystem the PayrollSystem instance to retrieve time logs
     * @param employee      the Employee object whose salary is to be calculated
     */
    public SalaryCalculationDialog(JFrame owner, PayrollSystem payrollSystem, Employee employee) {
        super(owner, "Calculate Salary", true);
        this.payrollSystem = payrollSystem;
        this.employee = employee;

        // Initialize month selector with full month names
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = Month.of(i + 1).getDisplayName(TextStyle.FULL, Locale.getDefault());
        }
        monthSelector = new JComboBox<>(months);

        // Initialize year selector with last 8 years including current
        Integer[] years = new Integer[8];
        int currentYear = YearMonth.now().getYear();
        for (int i = 0; i < years.length; i++) {
            years[i] = currentYear - i;
        }
        yearSelector = new JComboBox<>(years);

        // Initialize results panel with card-style layout
        JPanel temp = new JPanel();
        temp.setLayout(new GridLayout(0, 2, 12, 6));
        temp.setBackground(Theme.SURFACE);
        temp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(8, 10, 8, 10)));
        resultsPanel = temp;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 540));
        setLocationRelativeTo(owner);
        setContentPane(buildContent());

        // Set default month/year selection
        buildSelectors();
    }

    /**
     * Builds the header panel with the dialog title and employee info.
     *
     * @return the header component
     */
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(Theme.BACKGROUND);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildHeader() {
        JPanel header = Theme.cardPanel(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel title = new JLabel("Salary Calculation");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(employee.getFirstName() + " " + employee.getLastName() + "  •  " + employee.getEmployeeNumber());
        subtitle.setFont(Theme.SUBTITLE_FONT);
        subtitle.setForeground(Theme.TEXT_MUTED);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBackground(Theme.SURFACE);
        stack.add(title);
        stack.add(Box.createVerticalStrut(4));
        stack.add(subtitle);

        header.add(stack, BorderLayout.WEST);
        return header;
    }

    /**
     * Builds the body panel containing month/year selectors and results display.
     *
     * @return the body component
     */
    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);

        // Selector card (month/year + calculate button)
        JPanel selectors = Theme.cardPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        selectors.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(10, 12, 10, 12)));

        selectors.add(new JLabel("Month"));
        selectors.add(monthSelector);
        selectors.add(Box.createHorizontalStrut(6));
        selectors.add(new JLabel("Year"));
        selectors.add(yearSelector);
        selectors.add(Box.createHorizontalStrut(8));

        JButton calculateButton = Theme.createButton("Calculate", Theme.ACCENT);
        calculateButton.addActionListener(e -> calculate());
        selectors.add(calculateButton);

        body.add(selectors, BorderLayout.NORTH);

        // Results card (scrollable salary metrics)
        JPanel resultWrapper = Theme.cardPanel(new BorderLayout());
        resultWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(12, 12, 12, 12)));

        JScrollPane resultsScroll = new JScrollPane(resultsPanel);
        resultsScroll.setBorder(BorderFactory.createEmptyBorder());
        resultsScroll.getViewport().setBackground(Theme.SURFACE);
        resultsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        resultWrapper.add(resultsScroll, BorderLayout.CENTER);
        body.add(resultWrapper, BorderLayout.CENTER);

        return body;
    }

    /**
     * Builds the footer panel containing a close button.
     *
     * @return the footer component
     */
    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        JButton close = Theme.createButton("Close", Theme.DANGER);
        close.addActionListener(e -> dispose());
        footer.add(close);
        return footer;
    }

    /**
     * Sets default selections for month and year.
     */
    private void buildSelectors() {
        // Already instantiated in constructor; here we can set defaults if needed
        monthSelector.setSelectedIndex(YearMonth.now().getMonthValue() - 1);
        yearSelector.setSelectedItem(YearMonth.now().getYear());
    }

    /**
     * Calculates salary for the selected month/year and renders results.
     */
    private void calculate() {
        int month = monthSelector.getSelectedIndex() + 1;
        int year = (int) yearSelector.getSelectedItem();

        // Fetch time logs for the employee during the selected period
        YearMonth ym = YearMonth.of(year, month);
        List<TimeLog> logs = payrollSystem.getEmployeeTimeLogs(
                employee.getEmployeeNumber(),
                ym.atDay(1),
                ym.atEndOfMonth());

        if (logs == null || logs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No attendance records for the selected period.",
                    "No Data",
                    JOptionPane.INFORMATION_MESSAGE);
            resultsPanel.removeAll();
            resultsPanel.revalidate();
            resultsPanel.repaint();
            return;
        }

        // Compute salary details
        SalaryCalculationResult result = PayrollCalculationService.calculateCompletePayroll(employee, logs);
        double totalPaidHours = PayrollCalculationService.computePaidHours(logs);
        double overtimeHours = PayrollCalculationService.computeOvertimeHours(logs);

        // Render the results on the UI
        renderResults(result, totalPaidHours, overtimeHours);
    }

    /**
     * Renders the calculated salary metrics in the results panel.
     *
     * @param result        the calculated payroll result
     * @param totalHours    total worked hours
     * @param overtimeHours overtime hours worked
     */
    private void renderResults(SalaryCalculationResult result, double totalHours, double overtimeHours) {
        resultsPanel.removeAll();
        resultsPanel.setLayout(new GridLayout(0, 2, 12, 6));

        // Add metrics with labels and formatted values
        resultsPanel.add(metricLabel("Monthly Base Salary"));
        resultsPanel.add(metricValue(currencyFormat.format(employee.getBasicSalary())));
        
        resultsPanel.add(metricLabel("Hours Worked"));
        resultsPanel.add(metricValue(String.format(Locale.getDefault(), "%.2f hrs", totalHours)));

        resultsPanel.add(metricLabel("Over Time"));
        resultsPanel.add(metricValue(String.format(Locale.getDefault(), "%.2f hrs", overtimeHours)));

        // Allowances
        resultsPanel.add(metricLabel("Rice Subsidy"));
        resultsPanel.add(metricValue(currencyFormat.format(employee.getRiceSubsidy())));

        resultsPanel.add(metricLabel("Phone Allowance"));
        resultsPanel.add(metricValue(currencyFormat.format(employee.getPhoneAllowance())));

        resultsPanel.add(metricLabel("Clothing Allowance"));
        resultsPanel.add(metricValue(currencyFormat.format(employee.getClothingAllowance())));

        // Deductions
        resultsPanel.add(metricLabel("SSS Contribution"));
        resultsPanel.add(metricValue(asNegative(result.getSSSContribution())));

        resultsPanel.add(metricLabel("PhilHealth Contribution"));
        resultsPanel.add(metricValue(asNegative(result.getPhilhealthContribution())));

        resultsPanel.add(metricLabel("Pag-IBIG Contribution"));
        resultsPanel.add(metricValue(asNegative(result.getPagibigContribution())));
        resultsPanel.add(metricLabel("Withholding Tax"));
        resultsPanel.add(metricValue(asNegative(result.getWithholdingTax())));

        // Final calculations
        resultsPanel.add(metricLabel("Gross Pay"));
        resultsPanel.add(metricValue(currencyFormat.format(result.getGrossPay())));

        resultsPanel.add(metricLabel("Total Deductions"));
        resultsPanel.add(metricValue(asNegative(result.getTotalDeductions())));

        resultsPanel.add(metricLabel("Net Pay"));
        resultsPanel.add(emphasisValue(currencyFormat.format(result.getNetPay())));

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    /** Helper method to create a styled label for a metric name */
    private JLabel metricLabel(String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.BODY_FONT);
        lbl.setForeground(Theme.TEXT_MUTED);
        return lbl;
    }

    /** Helper method to create a styled label for a metric value */
    private JLabel metricValue(String value) {
        JLabel val = new JLabel(value);
        val.setFont(Theme.BUTTON_FONT);
        val.setForeground(Theme.TEXT_PRIMARY);
        return val;
    }

    /** Helper method to create an emphasized label for important values (like Net Pay) */
    private JLabel emphasisValue(String value) {
        JLabel val = new JLabel(value);
        val.setFont(Theme.TITLE_FONT);
        val.setForeground(Theme.TEXT_PRIMARY);
        return val;
    }

    /** Formats a positive amount as negative for display (deductions) */
    private String asNegative(double amount) {
        String formatted = currencyFormat.format(amount);
        return amount > 0 ? ("-" + formatted) : formatted;
    }
}
