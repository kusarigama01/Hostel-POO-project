package com.hotel.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Stay {
        private UUID id;
        private LocalDateTime checkIn;
        private LocalDateTime checkOut;
        private LocalDateTime actualCheckIn;
        private LocalDateTime actualCheckOut;

        // Enregistre l'arrivée réelle du client maintenant
        public void checkIn() {
            this.actualCheckIn = LocalDateTime.now();
        }

        // Enregistre le départ réel du client maintenant
        public void checkOut() {
            this.actualCheckOut = LocalDateTime.now();
        }

        // Le client est présent si actualCheckIn a eu lieu, mais pas encore actualCheckOut
        public boolean isCurrentlyStaying() {
            return this.actualCheckIn != null && this.actualCheckOut == null;
        }
    }

