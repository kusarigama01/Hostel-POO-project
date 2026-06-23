package com.hotel.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationHistory {
    private UUID id;
    private String action;
    private LocalDateTime actionDate;
    private String notes;
}
