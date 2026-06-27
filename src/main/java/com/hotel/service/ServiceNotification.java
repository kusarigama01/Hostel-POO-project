package com.hotel.service;

import com.hotel.model.Invoice;
import com.hotel.model.Reservation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceNotification {
    private final List<String> messages;

    public ServiceNotification() {
        this.messages = new ArrayList<>();
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public String notifyReservationCreated(Reservation reservation) {
        String message = "Reservation " + reservation.getCode() + " created for " + reservation.getCustomer().getFullName();
        messages.add(message);
        System.out.println(message);
        return message;
    }

    public String notifyReservationCancelled(Reservation reservation) {
        String message = "Reservation " + reservation.getCode() + " cancelled";
        messages.add(message);
        System.out.println(message);
        return message;
    }

    public String notifyPayment(Invoice invoice) {
        String message = "Invoice " + invoice.getInvoiceNumber() + " paid";
        messages.add(message);
        System.out.println(message);
        return message;
    }
}
