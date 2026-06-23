package com.hotel.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ServiceOrder {

    private UUID id;
    private UUID stayId;
    private LocalDateTime orderDate;
    private int quantity;
    private LocalDateTime scheduledDate;
    private BigDecimal totalPrice;
    private ServiceOrderStatus status;

    public ServiceOrder(UUID stayId, Service service, int quantity, LocalDateTime scheduledDate) {
        this.id = UUID.randomUUID();
        this.stayId = stayId;
        this.orderDate = LocalDateTime.now();
        this.quantity = quantity;
        this.scheduledDate = scheduledDate;
        this.totalPrice = service.getPrice().multiply(BigDecimal.valueOf(quantity));
        this.status = ServiceOrderStatus.PENDING;
    }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public boolean isAvailable() { return status == ServiceOrderStatus.PENDING; }

    public UUID getId() { return id; }
    public UUID getStayId() { return stayId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public ServiceOrderStatus getStatus() { return status; }
    public void setStatus(ServiceOrderStatus status) { this.status = status; }
}