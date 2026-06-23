package com.hotel.model;

import com.hotel.exception.RoomNotAvailableException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Scheduling {
    private final UUID id;
    private final List<Reservation> reservations;

    public Scheduling() {
        this.id = UUID.randomUUID();
        this.reservations = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public void addReservation(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation must not be null");
        if (!isAvailable(reservation.getRoom(), reservation.getDateRange())) {
            throw new RoomNotAvailableException("Room " + reservation.getRoom().getNumber() + " is not available for the selected dates");
        }
        reservations.add(reservation);
    }

    public void removeReservation(UUID reservationId) {
        reservations.removeIf(reservation -> reservation.getId().equals(reservationId));
    }

    public void removeReservationsForRoom(UUID roomId) {
        reservations.removeIf(reservation -> reservation.getRoom().getId().equals(roomId));
    }

    public boolean isAvailable(Room room, DateRange dateRange) {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(dateRange, "dateRange must not be null");

        if (!room.isAvailable()) {
            return false;
        }

        return reservations.stream()
                .filter(Reservation::isActive)
                .filter(reservation -> reservation.getRoom().getId().equals(room.getId()))
                .noneMatch(reservation -> reservation.getDateRange().overlaps(dateRange));
    }

    public List<Reservation> getReservations(Room room, DateRange dateRange) {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(dateRange, "dateRange must not be null");

        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getRoom().getId().equals(room.getId()) && reservation.getDateRange().overlaps(dateRange)) {
                result.add(reservation);
            }
        }
        return result;
    }
}
