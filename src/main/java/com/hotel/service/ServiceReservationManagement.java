package com.hotel.service;

import com.hotel.exception.PaymentException;
import com.hotel.model.Reservation;
import com.hotel.model.Service;
import com.hotel.model.ServiceOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ServiceReservationManagement {
    private final List<ServiceOrder> orders;

    public ServiceReservationManagement() {
        this.orders = new ArrayList<>();
    }

    public List<ServiceOrder> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public ServiceOrder placeOrder(Reservation reservation, Service service, int quantity) {
        Objects.requireNonNull(reservation, "reservation must not be null");
        Objects.requireNonNull(service, "service must not be null");
        if (!service.isActive()) {
            throw new PaymentException("Inactive services cannot be ordered");
        }
        ServiceOrder order = new ServiceOrder(reservation, service, quantity);
        orders.add(order);
        return order;
    }

    public void cancelOrder(UUID orderId) {
        ServiceOrder order = findOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        order.cancel();
    }

    public ServiceOrder findOrder(UUID orderId) {
        for (ServiceOrder order : orders) {
            if (order.getId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    public BigDecimal getOrdersTotalForReservation(UUID reservationId) {
        return orders.stream()
                .filter(order -> order.getReservation().getId().equals(reservationId))
                .map(ServiceOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
