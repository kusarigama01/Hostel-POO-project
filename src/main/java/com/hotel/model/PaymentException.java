package com.hotel.model;

import com.hotel.exception.BusinessException;
import lombok.Getter;
import java.util.UUID;

@Getter
public class PaymentException extends BusinessException {
    private final UUID paymentId;
    private final String reason;

    public PaymentException(UUID paymentId, String reason) {
        super(reason);
        this.paymentId = paymentId;
        this.reason = reason;
    }
}