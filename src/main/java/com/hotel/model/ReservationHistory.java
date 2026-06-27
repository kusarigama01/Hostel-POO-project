package com.hotel.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationHistory {
    private UUID id;

    private Reservation reservation;
    private String action;
    private LocalDateTime actionDate;
    private String notes;

    public ReservationHistory(Reservation reservation, String action, LocalDateTime actionDate, String notes) {
        this.id = UUID.randomUUID();
        this.reservation = reservation;
        this.action = action;
        this.actionDate = actionDate;
        this.notes = notes;
    }
}
