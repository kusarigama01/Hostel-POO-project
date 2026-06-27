package com.hotel.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stay {
    private UUID id;
    private Reservation reservation;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;

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
        return this.actualCheckIn != null && this.actualCheckOut == null;
    }
}
