package com.hotel.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    private UUID id;
    private String invoiceNumber;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private Reservation reservation;

    public void generate() {
        this.id = UUID.randomUUID();
        this.issueDate = LocalDate.now();
        this.status = InvoiceStatus.UNPAID;
        this.invoiceNumber = "INV-" + System.currentTimeMillis();
    }

    public Invoice(Reservation reservation, BigDecimal totalAmount) {
        this.reservation = Objects.requireNonNull(reservation, "reservation must not be null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        generate();
    }

    // Passe le statut à PAID (Payé)
    public void markAsPaid() {
        this.status = InvoiceStatus.PAID;
    }
}
