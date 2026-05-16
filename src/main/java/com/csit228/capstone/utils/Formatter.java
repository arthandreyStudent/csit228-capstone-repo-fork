package com.csit228.capstone.utils;

import com.csit228.capstone.model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.paint.Color;

public class Formatter {

  public static String formatDate(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "N/A";
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
      "MMM d, yyyy h:mm a"
    );
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
        formatted.append(
          capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c)
        );
        capitalizeNext = false;
      }
    }

    return formatted.toString();
  }

  public static String getInitials(User user) {
    if (user == null) {
      return "NA";
    }

    String firstName = trimOrNA(user.getFirstName());
    String lastName = trimOrNA(user.getLastName());

    String firstInitial = firstName.equals("N/A")
      ? ""
      : firstName.substring(0, 1);
    String lastInitial = lastName.equals("N/A") ? "" : lastName.substring(0, 1);

    String initials = firstInitial + lastInitial;

    return initials.isBlank() ? "NA" : initials.toUpperCase();
  }

  public static String trimOrNA(String value) {
    if (value == null || value.trim().isEmpty()) {
      return "N/A";
    }

    return value.trim();
  }

  public static String formatPercent(double rate) {
    return Math.round(rate * 100) + "%";
  }

}
