package com.hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateRange {
    private LocalDate startDate;
    private LocalDate endDate;

    public DateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid Dates");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    public boolean includes(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean overlaps(DateRange other) {
        return !this.startDate.isAfter(other.getEndDate()) && !other.getStartDate().isAfter(this.endDate);
    }

    public long duration() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}