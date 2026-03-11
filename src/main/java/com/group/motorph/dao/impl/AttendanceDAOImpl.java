package com.group.motorph.dao.impl;

import com.group.motorph.dao.AttendanceDAO;
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

/**
 * CSV-based implementation of AttendanceDAO.
 */
public class AttendanceDAOImpl implements AttendanceDAO {

    private static final String ATTENDANCE_FILE = CSVHandler.getDataDirectory() + "attendance-record.csv";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    private static final String STATUS_APPROVED = "Approved";
    private static final String STATUS_PENDING = "Pending";

    public AttendanceDAOImpl() {
        CSVHandler.ensureDataDirectory();
        ensureStatusColumn();
    }

    @Override
    public void ensureStatusColumn() {
        try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {
            String header = br.readLine();
            if (header == null) return;
            if (header.toLowerCase().contains("status")) return;

            // rewrite with Status column = Pending
            List<AttendanceRecord> all = readAll();
            List<String[]> rows = new ArrayList<>();
            for (AttendanceRecord r : all) rows.add(toRow(r, STATUS_PENDING));

            CSVHandler.writeCSV(ATTENDANCE_FILE,
                    new String[]{"Employee #","Last Name","First Name","Date","Log In","Log Out","Status"},
                    rows);
        } catch (Exception ignored) {}
    }

    @Override
    public List<AttendanceRecord> getAllAttendance() {
        return readAll();
    }

    @Override
    public List<AttendanceRecord> getAttendanceByEmployeeId(String employeeId) {
        List<AttendanceRecord> out = new ArrayList<>();
        for (AttendanceRecord r : readAll()) if (safe(r.getEmployeeId()).equals(employeeId)) out.add(r);
        return out;
    }

    @Override
    public List<AttendanceRecord> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> out = new ArrayList<>();
        for (AttendanceRecord r : readAll()) {
            if (r.getDate() == null) continue;
            if ((r.getDate().isEqual(startDate) || r.getDate().isAfter(startDate))
                    && (r.getDate().isEqual(endDate) || r.getDate().isBefore(endDate))) {
                out.add(r);
            }
        }
        return out;
    }

    @Override
    public List<AttendanceRecord> getAttendanceByEmployeeAndMonth(String employeeId, int month, int year) {
        List<AttendanceRecord> out = new ArrayList<>();
        for (AttendanceRecord r : readAll()) {
            if (!safe(r.getEmployeeId()).equals(employeeId) || r.getDate() == null) continue;
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) out.add(r);
        }
        return out;
    }

    @Override
    public List<AttendanceRecord> getPendingAttendanceByMonth(int month, int year) {
        List<AttendanceRecord> out = new ArrayList<>();
        for (AttendanceRecord r : readAll()) {
            if (r.getDate() == null) continue;
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) {
                if (!STATUS_APPROVED.equalsIgnoreCase(safe(r.getStatus()).trim())) out.add(r);
            }
        }
        return out;
    }

    @Override
    public boolean addAttendanceRecord(AttendanceRecord record) {
        try {
            ensureStatusColumn();
            CSVHandler.appendToCSV(ATTENDANCE_FILE, toRow(record, defaultStatus(record)));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateAttendanceRecord(String employeeId, LocalDate oldDate, AttendanceRecord updated) {
        ensureStatusColumn();
        List<AttendanceRecord> all = readAll();
        List<String[]> rows = new ArrayList<>();
        boolean changed = false;

        for (AttendanceRecord r : all) {
            if (safe(r.getEmployeeId()).equals(employeeId) && r.getDate() != null && r.getDate().isEqual(oldDate)) {
                String status = safe(updated.getStatus()).trim().isEmpty() ? defaultStatus(r) : updated.getStatus();
                rows.add(toRow(updated, status));
                changed = true;
            } else {
                rows.add(toRow(r, defaultStatus(r)));
            }
        }

        if (changed) {
            CSVHandler.writeCSV(ATTENDANCE_FILE,
                    new String[]{"Employee #","Last Name","First Name","Date","Log In","Log Out","Status"},
                    rows);
        }
        return changed;
    }

    @Override
    public boolean deleteAttendanceRecord(String employeeId, LocalDate date) {
        ensureStatusColumn();
        List<AttendanceRecord> all = readAll();
        List<String[]> rows = new ArrayList<>();
        boolean deleted = false;

        for (AttendanceRecord r : all) {
            if (safe(r.getEmployeeId()).equals(employeeId) && r.getDate() != null && r.getDate().isEqual(date)) {
                deleted = true;
                continue;
            }
            rows.add(toRow(r, defaultStatus(r)));
        }

        if (deleted) {
            CSVHandler.writeCSV(ATTENDANCE_FILE,
                    new String[]{"Employee #","Last Name","First Name","Date","Log In","Log Out","Status"},
                    rows);
        }
        return deleted;
    }

    @Override
    public boolean markAttendanceApproved(int month, int year) {
        ensureStatusColumn();
        List<AttendanceRecord> all = readAll();
        List<String[]> rows = new ArrayList<>();
        boolean any = false;

        for (AttendanceRecord r : all) {
            if (r.getDate() != null && r.getDate().getMonthValue() == month && r.getDate().getYear() == year) {
                if (!STATUS_APPROVED.equalsIgnoreCase(safe(r.getStatus()).trim())) any = true;
                rows.add(toRow(r, STATUS_APPROVED));
            } else {
                rows.add(toRow(r, defaultStatus(r)));
            }
        }

        if (any) {
            CSVHandler.writeCSV(ATTENDANCE_FILE,
                    new String[]{"Employee #","Last Name","First Name","Date","Log In","Log Out","Status"},
                    rows);
        }
        return any;
    }

    private List<AttendanceRecord> readAll() {
        List<AttendanceRecord> out = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(ATTENDANCE_FILE);
        for (String[] row : data) {
            if (row.length < 6) continue;

            String empId = safe(row[0]).trim();
            String last = safe(row[1]).trim();
            String first = safe(row[2]).trim();
            LocalDate date = parseDate(row[3]);
            LocalTime in = parseTime(row[4]);
            LocalTime outTime = parseTime(row[5]);

            String status = STATUS_PENDING;
            if (row.length >= 7) status = safe(row[6]).trim();
            if (status.isEmpty()) status = STATUS_PENDING;

            out.add(new AttendanceRecord(empId, last, first, date, in, outTime, status));
        }
        return out;
    }

    private String[] toRow(AttendanceRecord r, String statusOverride) {
        return new String[]{
                safe(r.getEmployeeId()),
                safe(r.getLastName()),
                safe(r.getFirstName()),
                formatDate(r.getDate()),
                formatTime(r.getClockIn()),
                formatTime(r.getClockOut()),
                safe(statusOverride).trim().isEmpty() ? defaultStatus(r) : statusOverride
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

    private String defaultStatus(AttendanceRecord r) {
        String s = safe(r.getStatus()).trim();
        return s.isEmpty() ? STATUS_PENDING : s;
    }
}
