package com.group.motorph.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for delimited-file operations. This helper supports BOTH CSV
 * and TSV formats
 */
public class CSVHandler {

    // Read all lines from a CSV file
    public static List<String[]> readCSV(String filePath) {
        return readDelimited(filePath, ',', true);
    }

    // Read all lines from a delimited file (CSV/TSV).
    public static List<String[]> readDelimited(String filePath, char delimiter, boolean skipHeader) {
        List<String[]> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (skipHeader && isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                if (line.isEmpty()) {
                    continue;
                }

                String[] values = split(line, delimiter);
                data.add(values);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filePath);
            createEmptyFile(filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return data;
    }

    // Read a file and auto-detect delimiter based on extension. - .tsv => tab
    public static List<String[]> readAuto(String filePath) {
        return readDelimited(filePath, delimiterForPath(filePath), true);
    }

    // Write data to a CSV file
    public static void writeCSV(String filePath, String[] headers, List<String[]> data) {
        writeDelimited(filePath, headers, data, ',');
    }

    public static void writeDelimited(String filePath, String[] headers, List<String[]> data, char delimiter) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(String.join(String.valueOf(delimiter), headers));
            bw.newLine();

            for (String[] row : data) {
                bw.write(String.join(String.valueOf(delimiter), row));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    // Write a file and auto-select delimiter based on extension.
    public static void writeAuto(String filePath, String[] headers, List<String[]> data) {
        writeDelimited(filePath, headers, data, delimiterForPath(filePath));
    }

    // Append a row to CSV file
    public static void appendToCSV(String filePath, String[] data) {
        appendDelimited(filePath, data, ',');
    }

    public static void appendDelimited(String filePath, String[] data, char delimiter) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(String.join(String.valueOf(delimiter), data));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending to file: " + e.getMessage());
        }
    }

    public static void appendAuto(String filePath, String[] data) {
        appendDelimited(filePath, data, delimiterForPath(filePath));
    }

    // Create an empty file with directory structure
    private static void createEmptyFile(String filePath) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
    }

    // Get the data directory path
    public static String getDataDirectory() {
        return "resources/";
    }

    // Ensure data directory exists
    public static void ensureDataDirectory() {
        File directory = new File(getDataDirectory());
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private static char delimiterForPath(String filePath) {
        if (filePath == null) {
            return ',';
        }
        String lower = filePath.toLowerCase();
        return lower.endsWith(".tsv") ? '\t' : ',';
    }

    private static String[] split(String line, char delimiter) {
        // Keep empty columns
        if (delimiter == '\t') {
            return line.split("\\t", -1);
        }
        return line.split(",", -1);
    }
}
