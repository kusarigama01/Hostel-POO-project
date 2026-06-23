package com.hotel.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    // Initialise les données de la facture lors de sa création
    public void generate() {
        this.id = UUID.randomUUID();
        this.issueDate = LocalDate.now();
        this.status = InvoiceStatus.UNPAID; // Commande par défaut à payer
        // Le numéro de facture pourrait être généré dynamiquement (ex: FACT-2026-001)
        this.invoiceNumber = "INV-" + System.currentTimeMillis();
    }

    // Passe le statut à PAID (Payé)
    public void markAsPaid() {
        this.status = InvoiceStatus.PAID;
    }
}
