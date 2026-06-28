package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Invoice {
    private UUID id;
    private String invoiceNumber;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private Reservation reservation;


    public Invoice(UUID id, String invoiceNumber, LocalDate issueDate, BigDecimal totalAmount,
                   InvoiceStatus status, Reservation reservation) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.reservation = reservation;
    }

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }
}