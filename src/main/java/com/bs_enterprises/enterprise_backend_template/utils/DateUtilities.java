package com.bs_enterprises.enterprise_backend_template.utils;

import java.time.*;
import java.util.Objects;

public class DateUtilities {

    private DateUtilities() {
    }

    /**
     * Converts a value to Instant type.
     * Handles both Instant and String inputs.
     *
     * @param value the value to convert (can be Instant, String, or any other type)
     * @return the converted Instant value, or null if conversion is not possible
     */
    public static Instant convertToInstant(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Instant instant) {
            return instant;
        }

        if (value instanceof String string) {
            try {
                return Instant.parse(string);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Calculates tenure in a readable format (e.g., "2 years, 3 months, 5 days").
     * If endDate is null, uses current time as end date.
     *
     * @param startDate the start date as Instant
     * @param endDate the end date as Instant (if null, uses current time)
     * @return the tenure as a formatted string, or null if startDate is null
     */
    public static String calculateTenure(Instant startDate, Instant endDate) {
        if (startDate == null) {
            return null;
        }

        Instant effectiveEndDate = (endDate != null) ? endDate : Instant.now();

        if (startDate.isAfter(effectiveEndDate)) {
            return null;
        }

        // Convert to LocalDate to handle calendar math correctly
        LocalDate start = startDate.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate end = effectiveEndDate.atZone(ZoneOffset.UTC).toLocalDate();

        // Same day case → explicitly return 0
        if (start.equals(end)) {
            return formatTenure(0, 0, 0);
        }

        Period period = Period.between(start, end);

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        return formatTenure(years, months, days);
    }

    /**
     * Formats tenure components into a readable string.
     *
     * @param years the number of years
     * @param months the number of months
     * @param days the number of days
     * @return formatted tenure string
     */
    private static String formatTenure(long years, long months, long days) {
        StringBuilder tenure = new StringBuilder();

        if (years > 0) {
            tenure.append(years).append(years == 1 ? " year" : " years");
        }

        if (months > 0) {
            appendPart(tenure, months, " month");
        }

        if (days > 0) {
            appendPart(tenure, days, " day");
        }

        return tenure.isEmpty() ? "0 days" : tenure.toString();
    }

    /**
     * Appends a tenure component to the StringBuilder.
     *
     * @param tenure the StringBuilder to append to
     * @param value the component value
     * @param unit the unit name (singular)
     */
    private static void appendPart(StringBuilder tenure, long value, String unit) {
        if (!tenure.isEmpty()) {
            tenure.append(", ");
        }
        tenure.append(value).append(value == 1 ? unit : unit + "s");
    }

    public static LocalDate getLocalDateFromInstant(Instant instant) {
        if (Objects.isNull(instant)) {
            return null;
        }

        return instant
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
