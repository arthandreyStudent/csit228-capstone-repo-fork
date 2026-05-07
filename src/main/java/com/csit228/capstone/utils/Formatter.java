package com.csit228.capstone.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Formatter {
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
        return dateTime.format(formatter);
    }

    public static String formatDepartmentName(String departmentName) {
        if (departmentName == null || departmentName.isBlank()) {
            return departmentName;
        }

        StringBuilder formatted = new StringBuilder(departmentName.length());
        boolean capitalizeNext = true;

        for (char c : departmentName.toCharArray()) {
            if (Character.isWhitespace(c)) {
                formatted.append(c);
                capitalizeNext = true;
            } else {
                formatted.append(capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalizeNext = false;
            }
        }

        return formatted.toString();
    }
}
