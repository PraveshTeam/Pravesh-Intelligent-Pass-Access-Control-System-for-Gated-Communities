package com.pravesh.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateFormatUtil {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

    public static String format(String isoDateTime) {
        try {
            return LocalDateTime.parse(isoDateTime).format(DISPLAY_FORMAT);
        } catch (DateTimeParseException e) {
            return isoDateTime; 
        }
    }
}