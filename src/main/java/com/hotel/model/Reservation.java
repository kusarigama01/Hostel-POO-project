package com.hotel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Reservation {
    private final UUID id;
    private final String code;
    private final Customer customer;
    private final Room room;
    private final DateRange dateRange;
    private final LocalDateTime createdAt;
    private ReservationStatus status;
    private BigDecimal totalPrice;

    public Reservation(Customer customer, Room room, DateRange dateRange) {
        this.id = UUID.randomUUID();
        this.code = "RSV-" + id.toString().substring(0, 8).toUpperCase();
        this.customer = Objects.requireNonNull(customer, "customer must not be null");
        this.room = Objects.requireNonNull(room, "room must not be null");
        this.dateRange = Objects.requireNonNull(dateRange, "dateRange must not be null");
        this.createdAt = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
        this.totalPrice = calculateTotalPrice();
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Room getRoom() {
        return room;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal calculateTotalPrice() {
        long nights = Math.max(1L, dateRange.duration());
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public void refreshTotalPrice() {
        this.totalPrice = calculateTotalPrice();
    }

    public void confirm() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled reservation cannot be confirmed");
        }
        status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        status = ReservationStatus.CANCELLED;
    }

    public boolean isActive() {
        return status != ReservationStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return code + " | " + customer.getFullName() + " | " + room.getNumber() + " | " + status;
    }
}
