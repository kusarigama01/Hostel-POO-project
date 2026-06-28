package com.hotel.model;

import com.hotel.exception.BusinessException;
import java.util.UUID;

public class PaymentException extends BusinessException {
    private final UUID paymentId;
    private final String reason;

    public PaymentException(UUID paymentId, String reason) {
        super(reason);
        this.paymentId = paymentId;
        this.reason = reason;
    }

    public UUID getPaymentId() { return paymentId; }
    public String getReason() { return reason; }
}