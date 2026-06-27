package com.hotel.service;

import com.hotel.model.Reservation;
import com.hotel.model.ServiceOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ServiceOrder {
    private UUID id;
    private Reservation reservation;
    private LocalDateTime orderDate;
    private int quantity;
    private BigDecimal totalPrice;
    private ServiceOrderStatus status;

    public ServiceOrder(Reservation reservation, Service service, int quantity) {
        this.id = UUID.randomUUID();
        this.reservation = reservation;
        this.orderDate = LocalDateTime.now();
        this.quantity = quantity;
        this.totalPrice = service.getPrice().multiply(BigDecimal.valueOf(quantity));
        this.status = ServiceOrderStatus.PENDING;
    }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public boolean isAvailable() { return status == ServiceOrderStatus.PENDING; }
    public UUID getId() { return id; }
    public Reservation getReservation() { return reservation; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public int getQuantity() { return quantity; }
    public ServiceOrderStatus getStatus() { return status; }

    public void cancel() {
        this.status = ServiceOrderStatus.CANCELLED;
    }

    public void setStatus(ServiceOrderStatus status) { this.status = status; }

    public void markOrdered() {
        this.status = ServiceOrderStatus.ORDERED;
    }

    public void markDelivered() {
        this.status = ServiceOrderStatus.DELIVERED;
    }
}
