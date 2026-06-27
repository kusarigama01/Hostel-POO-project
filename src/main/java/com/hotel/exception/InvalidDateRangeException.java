package com.hotel.exception;

import com.hotel.model.DateRange;

public class InvalidDateRangeException extends BusinessException {

    private final DateRange dateRange;

    public InvalidDateRangeException(String message) {
        super(message);
        this.dateRange = null;
    }

    public InvalidDateRangeException(DateRange dateRange) {
        super(buildMessage(dateRange));
        this.dateRange = dateRange;
    }

    private static String buildMessage(DateRange dateRange) {
        if (dateRange == null) {
            return "Invalid date range";
        }
        return "Invalid date range: " + dateRange.getStartDate() + " to " + dateRange.getEndDate();
    }

    public DateRange getDateRange() {
        return dateRange;
    }
}
