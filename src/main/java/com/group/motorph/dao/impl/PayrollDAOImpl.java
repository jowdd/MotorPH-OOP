package com.group.motorph.dao.impl;

import com.group.motorph.dao.PayrollDAO;
import com.group.motorph.model.PayrollRecord;
import com.group.motorph.util.CSVHandler;
import com.group.motorph.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV-based implementation of PayrollDAO
 * Demonstrates POLYMORPHISM
 */
public class PayrollDAOImpl implements PayrollDAO {
    
    private static final String PAYROLL_FILE = CSVHandler.getDataDirectory() + "payroll_records.csv";
    private static final String[] HEADERS = {
        "PayrollID", "EmployeeID", "Month", "Year", "HoursWorked", "OvertimeHours",
        "GrossPay", "SSS", "PhilHealth", "PagIbig", "WithholdingTax", 
        "TotalDeductions", "NetPay", "GeneratedDate"
    };
    
    public PayrollDAOImpl() {
        CSVHandler.ensureDataDirectory();
    }
    
    @Override
    public List<PayrollRecord> getAllPayrollRecords() {
        List<PayrollRecord> records = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(PAYROLL_FILE);
        
        for (String[] row : data) {
            if (row.length >= 14) {
                try {
                    PayrollRecord record = new PayrollRecord();
                    record.setPayslipId(row[0].trim());
                    record.setEmployeeId(row[1].trim());
                    record.setMonth(Integer.parseInt(row[2].trim()));
                    record.setYear(Integer.parseInt(row[3].trim()));
                    record.setHoursWorked(parseDouble(row[4]));
                    record.setOvertimeHours(parseDouble(row[5]));
                    record.setGrossPay(parseDouble(row[6]));
                    record.setSss(parseDouble(row[7]));
                    record.setPhilHealth(parseDouble(row[8]));
                    record.setPagIbig(parseDouble(row[9]));
                    record.setWithholdingTax(parseDouble(row[10]));
                    record.setTotalDeductions(parseDouble(row[11]));
                    record.setNetPay(parseDouble(row[12]));
                    record.setGeneratedDate(DateTimeUtil.parseDate(row[13].trim()));
                    records.add(record);
                } catch (Exception e) {
                    System.err.println("Error parsing payroll record: " + e.getMessage());
                }
            }
        }
        
        return records;
    }
    
    @Override
    public List<PayrollRecord> getPayrollByEmployeeId(String employeeId) {
        return getAllPayrollRecords().stream()
            .filter(record -> record.getEmployeeId().equals(employeeId))
            .collect(Collectors.toList());
    }
    
    @Override
    public PayrollRecord getPayrollByEmployeeAndPeriod(String employeeId, int month, int year) {
        return getAllPayrollRecords().stream()
            .filter(record -> record.getEmployeeId().equals(employeeId) &&
                            record.getMonth() == month &&
                            record.getYear() == year)
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public boolean addPayrollRecord(PayrollRecord record) {
        try {
            String[] data = {
                record.getPayslipId(),
                record.getEmployeeId(),
                String.valueOf(record.getMonth()),
                String.valueOf(record.getYear()),
                String.format("%.2f", record.getHoursWorked()),
                String.format("%.2f", record.getOvertimeHours()),
                String.format("%.2f", record.getGrossPay()),
                String.format("%.2f", record.getSss()),
                String.format("%.2f", record.getPhilHealth()),
                String.format("%.2f", record.getPagIbig()),
                String.format("%.2f", record.getWithholdingTax()),
                String.format("%.2f", record.getTotalDeductions()),
                String.format("%.2f", record.getNetPay()),
                DateTimeUtil.formatDate(record.getGeneratedDate())
            };
            CSVHandler.appendToCSV(PAYROLL_FILE, data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updatePayrollRecord(PayrollRecord record) {
        List<PayrollRecord> records = getAllPayrollRecords();
        List<String[]> data = new ArrayList<>();
        
        boolean updated = false;
        for (PayrollRecord rec : records) {
            if (rec.getPayslipId().equals(record.getPayslipId())) {
                data.add(new String[]{
                    record.getPayslipId(),
                    record.getEmployeeId(),
                    String.valueOf(record.getMonth()),
                    String.valueOf(record.getYear()),
                    String.format("%.2f", record.getHoursWorked()),
                    String.format("%.2f", record.getOvertimeHours()),
                    String.format("%.2f", record.getGrossPay()),
                    String.format("%.2f", record.getSss()),
                    String.format("%.2f", record.getPhilHealth()),
                    String.format("%.2f", record.getPagIbig()),
                    String.format("%.2f", record.getWithholdingTax()),
                    String.format("%.2f", record.getTotalDeductions()),
                    String.format("%.2f", record.getNetPay()),
                    DateTimeUtil.formatDate(record.getGeneratedDate())
                });
                updated = true;
            } else {
                data.add(new String[]{
                    rec.getPayslipId(),
                    rec.getEmployeeId(),
                    String.valueOf(rec.getMonth()),
                    String.valueOf(rec.getYear()),
                    String.format("%.2f", rec.getHoursWorked()),
                    String.format("%.2f", rec.getOvertimeHours()),
                    String.format("%.2f", rec.getGrossPay()),
                    String.format("%.2f", rec.getSss()),
                    String.format("%.2f", rec.getPhilHealth()),
                    String.format("%.2f", rec.getPagIbig()),
                    String.format("%.2f", rec.getWithholdingTax()),
                    String.format("%.2f", rec.getTotalDeductions()),
                    String.format("%.2f", rec.getNetPay()),
                    DateTimeUtil.formatDate(rec.getGeneratedDate())
                });
            }
        }
        
        if (updated) {
            CSVHandler.writeCSV(PAYROLL_FILE, HEADERS, data);
        }
        
        return updated;
    }
    
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
