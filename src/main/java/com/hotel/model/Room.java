package com.hotel.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Room {
    private final UUID id;
    private final String number;
    private final RoomType roomType;
    private BigDecimal pricePerNight;
    private RoomStatus status;

    public Room(String number, RoomType roomType, BigDecimal pricePerNight) {
        this(number, roomType, pricePerNight, RoomStatus.AVAILABLE);
    }

    public Room(String number, RoomType roomType, BigDecimal pricePerNight, RoomStatus status) {
        this.id = UUID.randomUUID();
        this.number = Objects.requireNonNull(number, "number must not be null");
        this.roomType = Objects.requireNonNull(roomType, "roomType must not be null");
        this.pricePerNight = Objects.requireNonNull(pricePerNight, "pricePerNight must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public UUID getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = Objects.requireNonNull(pricePerNight, "pricePerNight must not be null");
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }

    public boolean isOperational() {
        return status != RoomStatus.OUT_OF_ORDER;
    }

    @Override
    public String toString() {
        return number + " [" + roomType + ", " + pricePerNight + ", " + status + "]";
    }
}
