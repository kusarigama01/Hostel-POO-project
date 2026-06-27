package com.hotel.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    @Test
    void testGestionStatutService() {
        // Enregistrement d'un service (ex: Spa)
        var service = new Service("Spa", "Accès détente 1h", new BigDecimal("50.00"));

        // Vérifications initiales
        assertTrue(service.isActive());
        assertEquals("Spa", service.getName());

        // Test de désactivation / annulation
        service.cancel();
        assertFalse(service.isActive());

        // Test de réactivation
        service.activate();
        assertTrue(service.isActive());
    }

    @Test
    void testCalculPrixCommandeService() {
        // Given (Données de départ)
        var servicePrix = new BigDecimal("15.50"); // Prix unitaire (ex: Petit-déjeuner)
        var service = new Service("Petit-déjeuner", "Buffet matinal", servicePrix);
        var fauxStayId = UUID.randomUUID(); // On isole le test sans avoir besoin de la vraie classe Stay
        var quantite = 3;
        var datePrevue = LocalDateTime.now().plusDays(1);

        // When (Action)
        var commande = new ServiceOrder(fauxStayId, service, quantite, datePrevue);

        // Then (Vérifications)
        var prixAttendu = servicePrix.multiply(BigDecimal.valueOf(quantite)); // 15.50 * 3 = 46.50
        assertEquals(prixAttendu, commande.getTotalPrice());
        assertEquals(ServiceOrderStatus.PENDING, commande.getStatus());
        assertTrue(commande.isAvailable());
        assertEquals(fauxStayId, commande.getStayId());
    }

    @Test
    void testChangementStatutCommande() {
        var service = new Service("Wifi Premium", "Haut débit", new BigDecimal("10.00"));
        var commande = new ServiceOrder(UUID.randomUUID(), service, 1, LocalDateTime.now());

        // Changement de statut vers IN_PROGRESS puis DELIVERED
        commande.setStatus(ServiceOrderStatus.IN_PROGRESS);
        assertEquals(ServiceOrderStatus.IN_PROGRESS, commande.getStatus());
        assertFalse(commande.isAvailable(), "La commande ne devrait plus être PENDING");

        commande.setStatus(ServiceOrderStatus.DELIVERED);
        assertEquals(ServiceOrderStatus.DELIVERED, commande.getStatus());
    }
}