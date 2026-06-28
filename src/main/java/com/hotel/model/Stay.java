package com.hotel.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Stay {

    private UUID id;
    private Reservation reservation;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;

    public Stay(UUID id, Reservation reservation, LocalDateTime checkIn,
                LocalDateTime checkOut, LocalDateTime actualCheckIn,
                LocalDateTime actualCheckOut) {
        this.id = id;
        this.reservation = reservation;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.actualCheckIn = actualCheckIn;
        this.actualCheckOut = actualCheckOut;
    }

    public Stay(Reservation reservation) {
        this.id = UUID.randomUUID();
        this.reservation = Objects.requireNonNull(reservation, "reservation must not be null");
        this.checkIn = reservation.getDateRange().getStartDate().atStartOfDay();
        this.checkOut = reservation.getDateRange().getEndDate().atStartOfDay();
    }

    public void checkIn() {
        this.actualCheckIn = LocalDateTime.now();
    }

    public void checkOut() {
        this.actualCheckOut = LocalDateTime.now();
    }

    public boolean isCurrentlyStaying() {
        return actualCheckIn != null && actualCheckOut == null;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public LocalDateTime getActualCheckIn() {
        return actualCheckIn;
    }

    public LocalDateTime getActualCheckOut() {
        return actualCheckOut;
    }

    // Setters

    public void setId(UUID id) {
        this.id = id;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public void setActualCheckIn(LocalDateTime actualCheckIn) {
        this.actualCheckIn = actualCheckIn;
    }

    public void setActualCheckOut(LocalDateTime actualCheckOut) {
        this.actualCheckOut = actualCheckOut;
    }
}