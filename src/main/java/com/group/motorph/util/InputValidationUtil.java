package com.group.motorph.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation and formatting. Users can input numbers
 * without dashes, this class auto-formats them.
 */
public class InputValidationUtil {

    // Employee ID: numeric (e.g., 10001)
    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("\\d{4,6}");
    // SSS: 2-7-1 (e.g., 44-4506057-3)
    private static final Pattern SSS_PATTERN = Pattern.compile("\\d{2}-\\d{7}-\\d");
    // PAG-IBIG: 12 raw digits
    private static final Pattern PAGIBIG_PATTERN = Pattern.compile("\\d{12}");
    // PHILHEALTH: 12 raw digits
    private static final Pattern PHILHEALTH_PATTERN = Pattern.compile("\\d{12}");
    // TIN: 3-3-3-3 (e.g., 442-605-657-000)
    private static final Pattern TIN_PATTERN = Pattern.compile("\\d{3}-\\d{3}-\\d{3}-\\d{3}");

    // Returns true if the ID is a valid 4-6 digit number (e.g. 10001)
    public static boolean isValidEmployeeId(String id) {
        if (id == null) {
            return false;
        }
        return EMPLOYEE_ID_PATTERN.matcher(id.trim()).matches();
    }

    // Generates the next numeric employee ID string
    public static String formatEmployeeId(int nextNumber) {
        return String.valueOf(nextNumber);
    }

    // Format raw string to SSS: 44-4506057-3
    public static String formatSSS(String input) {
        if (input == null) {
            return "";
        }
        String digits = input.replaceAll("[^\\d]", "");
        if (digits.length() != 10) {
            return input.trim();
        }
        return digits.substring(0, 2) + "-" + digits.substring(2, 9) + "-" + digits.charAt(9);
    }

    public static boolean isValidSSS(String sss) {
        if (sss == null) {
            return false;
        }
        return SSS_PATTERN.matcher(sss.trim()).matches();
    }

    // Format raw string to PAG-IBIG: 12 plain digits (no dashes)
    public static String formatPagIbig(String input) {
        if (input == null) {
            return "";
        }
        String digits = input.replaceAll("[^\\d]", "");
        if (digits.length() != 12) {
            return input.trim();
        }
        return digits;
    }

    public static boolean isValidPagIbig(String pagibig) {
        if (pagibig == null) {
            return false;
        }
        return PAGIBIG_PATTERN.matcher(pagibig.trim()).matches();
    }

    // Format raw digits to PhilHealth (12 digits, no dashes)
    public static String formatPhilHealth(String input) {
        if (input == null) {
            return "";
        }
        String digits = input.replaceAll("[^\\d]", "");
        if (digits.length() != 12) {
            return input.trim();
        }
        return digits;
    }

    // Returns true if the PhilHealth number is exactly 12 digits
    public static boolean isValidPhilHealth(String ph) {
        if (ph == null) {
            return false;
        }
        return PHILHEALTH_PATTERN.matcher(ph.trim()).matches();
    }

    // Format raw string to TIN: 442-605-657-000
    public static String formatTIN(String input) {
        if (input == null) {
            return "";
        }
        String digits = input.replaceAll("[^\\d]", "");
        if (digits.length() != 12) {
            return input.trim();
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-"
                + digits.substring(6, 9) + "-" + digits.substring(9);
    }

    public static boolean isValidTIN(String tin) {
        if (tin == null) {
            return false;
        }
        return TIN_PATTERN.matcher(tin.trim()).matches();
    }

    /**
     * Converts a string to title case (each word capitalized). e.g. "juan dela
     * cruz" to "Juan Dela Cruz"
     */
    public static String toTitleCase(String s) {
        if (s == null || s.isBlank()) {
            return s == null ? "" : s;
        }
        String[] words = s.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    // Returns true if not null and not blank
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Trims a value and returns empty string if null
    public static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
