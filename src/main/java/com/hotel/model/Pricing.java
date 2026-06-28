package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Pricing {
    private UUID id;
    private RoomType roomType;
    private BigDecimal basePrice;
    private LocalDate validFrom;
    private LocalDate validTo;

    public Pricing() {
    }

    public Pricing(UUID id, RoomType roomType, BigDecimal basePrice, LocalDate validFrom, LocalDate validTo) {
        this.id = id;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public boolean isValid(LocalDate date) {
        if (date == null || validFrom == null || validTo == null) {
            return false;
        }
        return !date.isBefore(validFrom) && !date.isAfter(validTo);
    }

    public BigDecimal getPriceFor(LocalDate date) {
        if (isValid(date)) {
            return this.basePrice;
        }
        return BigDecimal.ZERO;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
}