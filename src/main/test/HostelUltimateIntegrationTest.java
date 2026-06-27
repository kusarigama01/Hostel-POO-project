package com.hotel.model;

import com.hotel.exception.RoomNotAvailableException;
import com.hotel.exception.PaymentException;
import com.hotel.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HostelUltimateIntegrationTest {

    private Hostel hostel;
    private Address hotelAddress;
    private ServiceRoomManagement roomService;
    private ServiceNotification notificationService;
    private ServiceReservationManagement orderManagement;

    @BeforeEach
    void initAll() {
        // Initialisation de l'infrastructure et des services du système
        hotelAddress = new Address("42 Avenue des Développeurs", "Paris", "IDF", "75011", "France");
        hostel = new Hostel("Java Ultimate Hostel", hotelAddress, "+33140404040", "ops@javahostel.com");

        roomService = new ServiceRoomManagement();
        notificationService = new ServiceNotification();
        orderManagement = new ServiceReservationManagement();
    }

    @Test
    @DisplayName("Scénario d'Exploitation Global : Configuration, Réservations, Incidents et Services Additionnels")
    void scenarioExploitationUltime() {

        // ==========================================
        // 1. CONFIGURATION DE LA STRUCTURE DE L'HOSTEL
        // ==========================================
        var rdc = new Floor(0);
        var premierEtage = new Floor(1);

        // Validation des exceptions de structure
        assertThrows(IllegalArgumentException.class, () -> new Floor(-1));

        var ch001 = new Room("001", RoomType.SINGLE, new BigDecimal("75.00"));
        var ch002 = new Room("002", RoomType.SINGLE, new BigDecimal("75.00"));
        var ch101 = new Room("101", RoomType.SUITE, new BigDecimal("250.00"));
        var ch102 = new Room("102", RoomType.DELUXE, new BigDecimal("150.00"));

        rdc.addRoom(ch001);
        rdc.addRoom(ch002);
        premierEtage.addRoom(ch101);
        premierEtage.addRoom(ch102);

        // Doublon sur le même étage
        assertThrows(IllegalArgumentException.class, () -> rdc.addRoom(new Room("001", RoomType.DELUXE, new BigDecimal("90.00"))));

        hostel.addFloor(rdc);
        hostel.addFloor(premierEtage);

        // Doublon d'étage sur l'Hostel
        assertThrows(IllegalArgumentException.class, () -> hostel.addFloor(new Floor(0)));

        // Vérifications structurelles des getters de collections immuables
        assertEquals(2, hostel.getFloors().size());
        assertEquals(4, hostel.getAllRooms().size());
        assertEquals(rdc, hostel.getFloor(0));
        assertNull(hostel.getFloor(99));

        // Test des recherches de chambres
        assertEquals(ch101, hostel.findRoom("101"));
        assertEquals(ch002, hostel.findRoomById(ch002.getId()));
        assertNull(hostel.findRoom("999"));
        assertNull(hostel.findRoomById(UUID.randomUUID()));

        // ==========================================
        // 2. CLIENTS ET PLAGES DE DATES
        // ==========================================
        var client1 = new Customer("Alice", "Rousseau", "alice@email.com", "+33611112222", LocalDate.of(1993, 4, 15), hotelAddress);
        var client2 = new Customer("Bob", "Sponge", "bob@email.com", "+33633334444", LocalDate.of(1986, 7, 20), hotelAddress);

        var rangeJuillet = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10));
        var rangeOverlap = new DateRange(LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 12));
        var rangeAout = new DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        // Vérification des états par défaut
        assertTrue(ch101.isAvailable());
        assertTrue(ch101.isOperational());
        assertTrue(hostel.getScheduling().isAvailable(ch101, rangeJuillet));

        // ==========================================
        // 3. CYCLE DE RÉSERVATION ET CONFLITS (SCHEDULING)
        // ==========================================

        // Réservation directe par numéro de chambre
        var res1 = hostel.createReservation(client1, "101", rangeJuillet);
        assertNotNull(res1);
        assertEquals(ReservationStatus.CONFIRMED, res1.getStatus());
        assertTrue(res1.isActive());
        assertNotNull(res1.getCreatedAt());

        // Notification automatique
        var normalMsg = notificationService.notifyReservationCreated(res1);
        assertTrue(normalMsg.contains(res1.getCode()));

        // Calcul automatique du prix : 9 nuits * 250.00 = 2250.00
        var prixAttenduRes1 = new BigDecimal("2250.00");
        assertEquals(prixAttenduRes1, res1.getTotalPrice());

        // Test de conflit d'occupation aux mêmes dates (doit échouer)
        assertThrows(RoomNotAvailableException.class, () -> hostel.createReservation(client2, "101", rangeOverlap));

        // Réservation automatique par type disponible
        var res2 = hostel.createReservation(client2, RoomType.SINGLE, rangeJuillet);
        assertNotNull(res2);
        // Doit choisir la première disponible (001)
        assertEquals("001", res2.getRoom().getNumber());

        // Plus de chambres standards de libre si on sature le créneau
        var res3 = hostel.createReservation(client1, RoomType.SINGLE, rangeJuillet);
        assertEquals("002", res3.getRoom().getNumber());

        assertThrows(RoomNotAvailableException.class, () -> hostel.createReservation(client2, RoomType.SINGLE, rangeJuillet));

        // Récupération des plannings croisés
        var reservationsCroisees = hostel.getScheduling().getReservations(ch101, rangeOverlap);
        assertEquals(1, reservationsCroisees.size());

        // ==========================================
        // 4. MAINTENANCE DE CHAMBRE ET CONSÉQUENCES
        // ==========================================

        // ch102 est libre en août
        assertEquals(1, hostel.getAvailableRooms(RoomType.DELUXE, rangeAout).size());

        // Passage en maintenance via le service de gestion de chambres
        roomService.markMaintenance(ch102);
        assertEquals(RoomStatus.MAINTENANCE, ch102.getStatus());
        assertFalse(ch102.isAvailable());

        // Elle ne doit plus ressortir dans les moteurs de recherche de dispo
        assertEquals(0, hostel.getAvailableRooms(RoomType.DELUXE, rangeAout).size());

        // Remise en service opérationnelle
        roomService.markAvailable(ch102);
        assertTrue(ch102.isAvailable());
        assertEquals(1, hostel.getAvailableRooms(RoomType.DELUXE, rangeAout).size());

        // Passage Hors-Service complet
        roomService.markOutOfOrder(ch102);
        assertFalse(ch102.isOperational());

        // Remise à disposition définitive
        roomService.markAvailable(ch102);

        // ==========================================
        // 5. COMMANDES DE SERVICES ADDITIONNELS (SERVICE & ORDERS)
        // ==========================================
        var petitDej = new Service("Petit-Déjeuner", new BigDecimal("15.00"), true);
        var spa = new Service("Accès Spa Privé", new BigDecimal("50.00"), false); // Inactif

        // Commande valide pour Alice (res1)
        var commande1 = orderManagement.placeOrder(res1, petitDej, 2);
        assertNotNull(commande1);
        assertEquals(new BigDecimal("30.00"), commande1.getTotalPrice());
        assertEquals(ServiceOrderStatus.PENDING, commande1.getStatus());

        // Changement adaptatif : on vérifie l'UUID de la réservation liée à la commande
        assertEquals(res1.getId(), commande1.getReservationId());

        // Tentative de commande d'un service inactif (Doit lever PaymentException grâce au UUID)
        assertThrows(PaymentException.class, () -> orderManagement.placeOrder(res1, spa, 1));

        // Ajout d'une deuxième commande
        var commande2 = orderManagement.placeOrder(res1, petitDej, 1);

        // Totalisation cumulée des services pour cette réservation : 30.00 + 15.00 = 45.00
        assertEquals(new BigDecimal("45.00"), orderManagement.getOrdersTotalForReservation(res1.getId()));

        // Annulation d'une commande
        orderManagement.cancelOrder(commande2.getId());
        assertEquals(ServiceOrderStatus.CANCELLED, commande2.getStatus());

        // Recherche de commande inexistante
        assertNull(orderManagement.findOrder(UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> orderManagement.cancelOrder(UUID.randomUUID()));

        // ==========================================
        // 6. SÉJOURS, ANNULATION ET FACTURATION (STAYS & INVOICES)
        // ==========================================

        // Enregistrement d'arrivée (Check-In)
        var stay1 = hostel.checkIn(res1.getId());
        assertNotNull(stay1);
        assertEquals(1, hostel.getStays().size());

        // Génération de facture
        var facture1 = hostel.generateInvoice(res1.getId());
        assertNotNull(facture1);
        assertEquals(res1.getTotalPrice(), facture1.getTotalAmount());

        // Simulation d'encaissement et calcul des revenus
        facture1.setStatus(InvoiceStatus.PAID);
        notificationService.notifyPayment(facture1);

        // Vérification de la caisse centrale de l'hostel
        assertEquals(prixAttenduRes1, hostel.getRevenue());

        // Départ du client (Check-Out)
        hostel.checkOut(res1.getId());

        // Scénario d'annulation d'une autre réservation (res2)
        hostel.cancelReservation(res2.getId());
        assertEquals(ReservationStatus.CANCELLED, res2.getStatus());
        assertFalse(res2.isActive());

        // Notification d'annulation
        var cancelMsg = notificationService.notifyReservationCancelled(res2);
        assertTrue(cancelMsg.contains(res2.getCode()));

        // Vérification finale des historiques de traçabilité
        assertFalse(hostel.getReservationHistory().isEmpty());

        // Test des toString()
        assertNotNull(res1.toString());
        assertNotNull(ch101.toString());
    }
}