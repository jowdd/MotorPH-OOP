package com.group.motorph.dao.impl;

import com.group.motorph.dao.ApprovedAttendanceDAO;
import com.group.motorph.model.AttendanceRecord;
import com.group.motorph.util.CSVHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ApprovedAttendanceDAOImpl implements ApprovedAttendanceDAO {

    private static final String APPROVED_FILE = CSVHandler.getDataDirectory() + "approved-attendance-logs.csv";
    private static final String[] HEADERS = {"Employee #","Last Name","First Name","Date","Log In","Log Out","Status"};

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    public ApprovedAttendanceDAOImpl() {
        CSVHandler.ensureDataDirectory();
        ensureHeader();
    }

    @Override
    public List<AttendanceRecord> getAllApproved() {
        ensureHeader();
        List<AttendanceRecord> out = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(APPROVED_FILE);

        for (String[] row : data) {
            if (row.length < 6) continue;

            String empId = safe(row[0]).trim();
            String last = safe(row[1]).trim();
            String first = safe(row[2]).trim();
            LocalDate date = parseDate(row[3]);
            LocalTime in = parseTime(row[4]);
            LocalTime outTime = parseTime(row[5]);

            String status = (row.length >= 7) ? safe(row[6]).trim() : "Approved";
            if (status.isEmpty()) status = "Approved";

            out.add(new AttendanceRecord(empId, last, first, date, in, outTime, status));
        }

        return out;
    }

    @Override
    public List<AttendanceRecord> getApprovedByMonth(int month, int year) {
        List<AttendanceRecord> out = new ArrayList<>();
        for (AttendanceRecord r : getAllApproved()) {
            if (r.getDate() == null) continue;
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) out.add(r);
        }
        return out;
    }

    @Override
    public void overwrite(List<AttendanceRecord> records) {
        ensureHeader();
        List<String[]> rows = new ArrayList<>();
        for (AttendanceRecord r : records) rows.add(toRow(r));
        CSVHandler.writeCSV(APPROVED_FILE, HEADERS, rows);
    }

    @Override
    public void clear() {
        CSVHandler.writeCSV(APPROVED_FILE, HEADERS, new ArrayList<>());
    }

    private void ensureHeader() {
        try (BufferedReader br = new BufferedReader(new FileReader(APPROVED_FILE))) {
            String header = br.readLine();
            if (header == null || header.trim().isEmpty()) {
                clear();
            }
        } catch (Exception e) {
            clear();
        }
    }

    private String[] toRow(AttendanceRecord r) {
        return new String[]{
                safe(r.getEmployeeId()),
                safe(r.getLastName()),
                safe(r.getFirstName()),
                formatDate(r.getDate()),
                formatTime(r.getClockIn()),
                formatTime(r.getClockOut()),
                safe(r.getStatus()).trim().isEmpty() ? "Approved" : r.getStatus()
        };
    }

    private String safe(String v) { return v == null ? "" : v; }

    private LocalDate parseDate(String s) {
        try { return LocalDate.parse(s.trim(), DATE_FMT); } catch (DateTimeParseException ex) { return null; }
    }

    private LocalTime parseTime(String s) {
        try { return LocalTime.parse(s.trim(), TIME_FMT); } catch (DateTimeParseException ex) { return null; }
    }

    private String formatDate(LocalDate d) { return d == null ? "" : d.format(DATE_FMT); }
    private String formatTime(LocalTime t) { return t == null ? "" : t.format(TIME_FMT); }
}
