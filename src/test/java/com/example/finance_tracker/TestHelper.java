package com.example.finance_tracker;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TestHelper {

    public static Date parseDate(String dateString) {
        // Parse the String into a LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(dateString, formatter);

        // Convert LocalDate to Date
        return java.sql.Date.valueOf(localDate);
    }
}