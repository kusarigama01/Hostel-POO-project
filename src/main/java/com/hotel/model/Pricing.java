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
public class Pricing {
    private UUID id;
    private RoomType roomType;
    private BigDecimal basePrice;
    private LocalDate validFrom;
    private LocalDate validTo;

    // Vérifie si la date est comprise entre validFrom (inclus) et validTo (inclus)
    public boolean isValid(LocalDate date) {
        if (date == null || validFrom == null || validTo == null) {
            return false;
        }
        return !date.isBefore(validFrom) && !date.isAfter(validTo);
    }

    // Retourne le prix si la date est valide, sinon on peut lever une exception ou retourner ZERO
    public BigDecimal getPriceFor(LocalDate date) {
        if (isValid(date)) {
            return this.basePrice;
        }
        return BigDecimal.ZERO;
    }
}
