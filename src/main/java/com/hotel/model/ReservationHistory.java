package com.hotel.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReservationHistory {
    private UUID id;
    private Reservation reservation;
    private String action;
    private LocalDateTime actionDate;
    private String notes;

    public ReservationHistory(UUID id, Reservation reservation, String action, LocalDateTime actionDate, String notes) {
        this.id = id;
        this.reservation = reservation;
        this.action = action;
        this.actionDate = actionDate;
        this.notes = notes;
    }

    public ReservationHistory(Reservation reservation, String action, LocalDateTime actionDate, String notes) {
        this.id = UUID.randomUUID();
        this.reservation = reservation;
        this.action = action;
        this.actionDate = actionDate;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}