
import com.hotel.exception.*;
import com.hotel.model.*;
import com.hotel.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HostelUltimateIntegrationTest {

    private Address sampleAddress;
    private Customer sampleCustomer;
    private Room sampleRoom;
    private DateRange sampleDateRange;
    private Hostel sampleHostel;
    private Manager sampleManager;

    @BeforeEach
    public void setUp() {
        sampleAddress = new Address("123 Rue de la Paix", "Paris", "Île-de-France", "75001", "France");
        sampleCustomer = new Customer("Jean", "Dupont", "jean.dupont@email.com", "+33612345678", LocalDate.of(1990, 5, 15), sampleAddress);
        sampleRoom = new Room("101", RoomType.SINGLE, new BigDecimal("85.00"), RoomStatus.AVAILABLE);
        sampleDateRange = new DateRange(LocalDate.now(), LocalDate.now().plusDays(3));
        sampleHostel = new Hostel("Le Grand Hostel", sampleAddress, "+33100000000", "contact@grandhostel.com");
        // sampleRoom est déjà rattaché à l'hôtel pour pouvoir tester directement
        // tout le cycle de réservation au niveau Hostel (sans le re-déclarer dans chaque test).
        sampleHostel.addFloor(new Floor(1));
        sampleHostel.addRoom(1, sampleRoom);
        sampleManager = new Manager("Alice", "Martin", "alice@grandhostel.com", "+33699999999", "EMP-001", sampleHostel);
    }

    // ==========================================
    // 1. TESTS SUR ADDRESS
    // ==========================================
    @Test
    public void testAddressGetFullAddress() {
        var expected = "123 Rue de la Paix, 75001 Paris, France";
        assertEquals(expected, sampleAddress.getFullAddress());
    }

    @Test
    public void testAddressWithDifferentValues() {
        var other = new Address("221B Baker Street", "London", "Greater London", "NW1 6XE", "United Kingdom");
        assertEquals("221B Baker Street, NW1 6XE London, United Kingdom", other.getFullAddress());
    }

    // ==========================================
    // 2. TESTS SUR DATERANGE
    // ==========================================
    @Test
    public void testDateRangeValidAndDuration() {
        var range = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5));
        assertEquals(4, range.duration());
        assertTrue(range.includes(LocalDate.of(2026, 7, 3)));
        assertFalse(range.includes(LocalDate.of(2026, 7, 6)));
    }

    @Test
    public void testDateRangeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DateRange(LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 1));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new DateRange(null, LocalDate.now());
        });
        // Alternative : endDate null également rejetée
        assertThrows(IllegalArgumentException.class, () -> {
            new DateRange(LocalDate.now(), null);
        });
    }

    @Test
    public void testDateRangeOverlaps() {
        var range1 = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5));
        var range2 = new DateRange(LocalDate.of(2026, 7, 4), LocalDate.of(2026, 7, 10));
        var range3 = new DateRange(LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 10));

        assertTrue(range1.overlaps(range2));
        assertFalse(range1.overlaps(range3)); // Turnover exclusif (le jour même du checkout)
        assertFalse(range3.overlaps(range1)); // La relation doit être symétrique
    }

    @Test
    public void testDateRangeOverlapsAlternatives() {
        var range = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10));

        // Identiques -> chevauchement
        assertTrue(range.overlaps(new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10))));

        // Totalement incluse -> chevauchement
        assertTrue(range.overlaps(new DateRange(LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 9))));

        // Totalement disjointe avant / après -> pas de chevauchement
        assertFalse(range.overlaps(new DateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5))));
        assertFalse(range.overlaps(new DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5))));

        // Plage ponctuelle (start == end) collée à la borne -> pas de chevauchement
        var point = new DateRange(LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 10));
        assertFalse(range.overlaps(point));
        assertFalse(point.overlaps(range));
    }

    @Test
    public void testDateRangeIncludesBoundariesAndZeroLength() {
        var range = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10));
        assertTrue(range.includes(range.getStartDate()));
        assertTrue(range.includes(range.getEndDate()));
        assertFalse(range.includes(range.getStartDate().minusDays(1)));

        var zeroLength = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1));
        assertEquals(0, zeroLength.duration());
        assertTrue(zeroLength.includes(LocalDate.of(2026, 7, 1)));
    }

    // ==========================================
    // 3. TESTS SUR FLOOR & ROOM
    // ==========================================
    @Test
    public void testFloorOperations() {
        var floor = new Floor(1);
        assertEquals(1, floor.getNumber());
        assertNotNull(floor.getId());

        floor.addRoom(sampleRoom);
        assertEquals(1, floor.getRooms().size());
        assertEquals(sampleRoom, floor.getRoomByNumber("101"));

        // Alternative : doublon de chambre
        assertThrows(IllegalArgumentException.class, () -> {
            floor.addRoom(new Room("101", RoomType.DOUBLE, new BigDecimal("120.00")));
        });

        // Suppression par numéro
        var removed = floor.removeRoom("101");
        assertEquals(sampleRoom, removed);
        assertNull(floor.getRoomByNumber("101"));
    }

    @Test
    public void testFloorRemoveByUuid() {
        var floor = new Floor(2);
        floor.addRoom(sampleRoom);

        var removed = floor.removeRoom(sampleRoom.getId());
        assertEquals(sampleRoom, removed);
        assertTrue(floor.getRooms().isEmpty());
    }

    @Test
    public void testFloorAlternativesAndEdgeCases() {
        var floor = new Floor(0); // étage 0 (rez-de-chaussée) doit être accepté
        assertEquals(0, floor.getNumber());
        assertThrows(IllegalArgumentException.class, () -> new Floor(-1));

        assertThrows(NullPointerException.class, () -> floor.addRoom(null));

        // Suppression d'une chambre/uuid inexistant -> null, pas d'exception
        assertNull(floor.removeRoom("999"));
        assertNull(floor.removeRoom(UUID.randomUUID()));
        assertThrows(NullPointerException.class, () -> floor.removeRoom((UUID) null));

        // getRooms() est non-modifiable
        floor.addRoom(sampleRoom);
        var rooms = floor.getRooms();
        assertThrows(UnsupportedOperationException.class, () -> rooms.add(sampleRoom));
    }

    @Test
    public void testRoomStatusAndPrice() {
        sampleRoom.setPricePerNight(new BigDecimal("90.00"));
        assertEquals(0, new BigDecimal("90.00").compareTo(sampleRoom.getPricePerNight()));

        assertTrue(sampleRoom.isAvailable());
        assertTrue(sampleRoom.isOperational());

        sampleRoom.setStatus(RoomStatus.OUT_OF_ORDER);
        assertFalse(sampleRoom.isAvailable());
        assertFalse(sampleRoom.isOperational());
    }

    @Test
    public void testRoomMaintenanceIsOperationalButNotAvailable() {
        // MAINTENANCE n'est pas AVAILABLE, mais reste "opérationnelle" (pas hors-service)
        sampleRoom.setStatus(RoomStatus.MAINTENANCE);
        assertFalse(sampleRoom.isAvailable());
        assertTrue(sampleRoom.isOperational());
    }

    @Test
    public void testRoomConstructorsAndNullChecks() {
        // Constructeur 3 arguments -> statut par défaut AVAILABLE
        var room = new Room("201", RoomType.TWIN, new BigDecimal("60.00"));
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());

        // Constructeur 4 arguments -> statut explicite
        var outOfOrderRoom = new Room("202", RoomType.TWIN, new BigDecimal("60.00"), RoomStatus.OUT_OF_ORDER);
        assertEquals(RoomStatus.OUT_OF_ORDER, outOfOrderRoom.getStatus());

        assertThrows(NullPointerException.class, () -> new Room(null, RoomType.SINGLE, new BigDecimal("10.00")));
        assertThrows(NullPointerException.class, () -> new Room("301", null, new BigDecimal("10.00")));
        assertThrows(NullPointerException.class, () -> new Room("301", RoomType.SINGLE, null));
        assertThrows(NullPointerException.class, () -> new Room("301", RoomType.SINGLE, new BigDecimal("10.00"), null));

        assertThrows(NullPointerException.class, () -> sampleRoom.setPricePerNight(null));
        assertThrows(NullPointerException.class, () -> sampleRoom.setStatus(null));
    }

    @Test
    public void testRoomToString() {
        var text = sampleRoom.toString();
        assertTrue(text.contains("101"));
        assertTrue(text.contains("SINGLE"));
        assertTrue(text.contains("AVAILABLE"));
    }

    // ==========================================
    // 4. TESTS SUR RESERVATION
    // ==========================================
    @Test
    public void testReservationLifecycleAndPricing() {
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        assertNotNull(reservation.getCode());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertTrue(reservation.isActive());

        // 3 nuits * 85.00 = 255.00
        var expectedPrice = new BigDecimal("255.00");
        assertEquals(0, expectedPrice.compareTo(reservation.getTotalPrice()));

        // Rafraîchissement du prix après modification de la chambre
        sampleRoom.setPricePerNight(new BigDecimal("100.00"));
        reservation.refreshTotalPrice();
        assertEquals(0, new BigDecimal("300.00").compareTo(reservation.getTotalPrice()));

        // Confirmation
        reservation.confirm();
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());

        // Annulation
        reservation.cancel();
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertFalse(reservation.isActive());

        // Alternative : Confirmer une réservation annulée
        assertThrows(IllegalStateException.class, reservation::confirm);
    }

    @Test
    public void testReservationConstructorNullChecks() {
        assertThrows(NullPointerException.class, () -> new Reservation(null, sampleRoom, sampleDateRange));
        assertThrows(NullPointerException.class, () -> new Reservation(sampleCustomer, null, sampleDateRange));
        assertThrows(NullPointerException.class, () -> new Reservation(sampleCustomer, sampleRoom, null));
    }

    @Test
    public void testReservationSameDayChargesAtLeastOneNight() {
        // start == end : duration() vaut 0, mais on facture au moins 1 nuit
        var sameDayRange = new DateRange(LocalDate.now(), LocalDate.now());
        var reservation = new Reservation(sampleCustomer, sampleRoom, sameDayRange);
        assertEquals(0, new BigDecimal("85.00").compareTo(reservation.getTotalPrice()));
    }

    @Test
    public void testReservationToStringContainsKeyInfo() {
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        var text = reservation.toString();
        assertTrue(text.contains(reservation.getCode()));
        assertTrue(text.contains(sampleCustomer.getFullName()));
        assertTrue(text.contains(sampleRoom.getNumber()));
        assertTrue(text.contains("PENDING"));
    }

    // ==========================================
    // 5. TESTS SUR INVOICE (création directe + via Hostel)
    // ==========================================
    @Test
    public void testInvoiceAndRevenue() {
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        var invoice = new Invoice(reservation, reservation.getTotalPrice());

        assertNotNull(invoice.getId());
        assertNotNull(invoice.getInvoiceNumber());
        assertEquals(InvoiceStatus.UNPAID, invoice.getStatus());
        assertEquals(reservation, invoice.getReservation());

        invoice.markAsPaid();
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
    }

    @Test
    public void testInvoiceConstructorNullChecks() {
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        assertThrows(NullPointerException.class, () -> new Invoice(null, reservation.getTotalPrice()));
        assertThrows(NullPointerException.class, () -> new Invoice(reservation, null));
    }

    @Test
    public void testHostelGenerateInvoiceAndPayInvoiceFlow() {
        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        var invoice = sampleHostel.generateInvoice(reservation.getId());

        assertEquals(0, reservation.getTotalPrice().compareTo(invoice.getTotalAmount()));
        assertEquals(InvoiceStatus.UNPAID, invoice.getStatus());
        assertEquals(invoice.getId(), sampleHostel.getInvoice(invoice.getId()).getId());

        // Alternative : facture pour une réservation inexistante
        assertThrows(IllegalArgumentException.class, () -> sampleHostel.generateInvoice(UUID.randomUUID()));

        // Alternative : facture inconnue
        assertNull(sampleHostel.getInvoice(UUID.randomUUID()));

        // Paiement réussi -> facture PAID + revenu mis à jour
        var successfulPayment = new Payment(UUID.randomUUID(), invoice.getTotalAmount(), LocalDateTime.now(), PaymentMethod.CREDIT_CARD, PaymentStatus.COMPLETED);
        sampleHostel.payInvoice(invoice.getId(), successfulPayment);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(0, invoice.getTotalAmount().compareTo(sampleHostel.getRevenue()));

        // Alternative : paiement vers une facture inexistante
        assertThrows(IllegalArgumentException.class, () -> sampleHostel.payInvoice(UUID.randomUUID(), successfulPayment));

        // Alternative : paiement null
        assertThrows(NullPointerException.class, () -> sampleHostel.payInvoice(invoice.getId(), null));
    }

    @Test
    public void testHostelPayInvoiceWithFailedPaymentLeavesItUnpaid() {
        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        var invoice = sampleHostel.generateInvoice(reservation.getId());

        var failedPayment = new Payment(UUID.randomUUID(), invoice.getTotalAmount(), LocalDateTime.now(), PaymentMethod.CASH, PaymentStatus.FAILED);
        sampleHostel.payInvoice(invoice.getId(), failedPayment);

        assertEquals(InvoiceStatus.UNPAID, invoice.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(sampleHostel.getRevenue()));
    }

    @Test
    public void testHostelRevenueOnlySumsPaidInvoices() {
        var room2 = new Room("102", RoomType.DOUBLE, new BigDecimal("120.00"));
        sampleHostel.addRoom(1, room2);

        var reservation1 = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        var reservation2 = sampleHostel.createReservation(sampleCustomer, "102", sampleDateRange);

        var invoice1 = sampleHostel.generateInvoice(reservation1.getId());
        var invoice2 = sampleHostel.generateInvoice(reservation2.getId());

        sampleHostel.payInvoice(invoice1.getId(),
                new Payment(UUID.randomUUID(), invoice1.getTotalAmount(), LocalDateTime.now(), PaymentMethod.CASH, PaymentStatus.COMPLETED));
        // invoice2 reste UNPAID (aucun paiement)

        assertEquals(0, invoice1.getTotalAmount().compareTo(sampleHostel.getRevenue()));
        assertNotEquals(0, invoice1.getTotalAmount().add(invoice2.getTotalAmount()).compareTo(sampleHostel.getRevenue()));
    }

    // ==========================================
    // 6. TESTS SUR LE MANAGER
    // ==========================================
    @Test
    public void testManagerActionsWithoutHostel() {
        var standaloneManager = new Manager("Bob", "Sponge", "bob@email.com", "123", "EMP-002");
        assertNull(standaloneManager.getHostel());
        assertThrows(IllegalStateException.class, () -> standaloneManager.addRoom(1, sampleRoom));
        assertThrows(IllegalStateException.class, () -> standaloneManager.updateRoomPrice("101", new BigDecimal("10.00")));
        assertThrows(IllegalStateException.class, () -> standaloneManager.removeRoom(1, "101"));
        assertThrows(IllegalStateException.class, standaloneManager::manageReservations);
        assertThrows(IllegalStateException.class, standaloneManager::managePayments);
        assertThrows(IllegalStateException.class, standaloneManager::generateReports);

        // Alternative : on l'affecte ensuite à un hôtel -> ça fonctionne
        standaloneManager.setHostel(sampleHostel);
        assertEquals(sampleHostel, standaloneManager.getHostel());
        assertDoesNotThrow(standaloneManager::generateReports);
    }

    @Test
    public void testManagerGenerateReports() {
        var report = sampleManager.generateReports();
        assertTrue(report.contains("Le Grand Hostel"));
        assertTrue(report.contains("Revenue:"));
    }

    @Test
    public void testManagerUpdateRoomPrice() {
        sampleManager.updateRoomPrice("101", new BigDecimal("99.00"));
        assertEquals(0, new BigDecimal("99.00").compareTo(sampleRoom.getPricePerNight()));

        assertThrows(IllegalArgumentException.class, () -> sampleManager.updateRoomPrice("999", new BigDecimal("10.00")));
    }

    @Test
    public void testManagerAddAndRemoveRoom() {
        var room2 = new Room("103", RoomType.SUITE, new BigDecimal("150.00"));
        sampleManager.addRoom(1, room2);
        assertEquals(room2, sampleHostel.findRoom("103"));

        sampleManager.removeRoom(1, "103");
        assertNull(sampleHostel.findRoom("103"));

        // Alternative : étage inexistant
        assertThrows(IllegalArgumentException.class, () -> sampleManager.removeRoom(99, "101"));
    }

    @Test
    public void testManagerManageReservationsAndPayments() {
        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);

        List<Reservation> managed = sampleManager.manageReservations();
        assertEquals(sampleHostel.getReservations().size(), managed.size());
        assertTrue(managed.stream().anyMatch(r -> r.getId().equals(reservation.getId())));

        var invoice = sampleHostel.generateInvoice(reservation.getId());
        sampleHostel.payInvoice(invoice.getId(),
                new Payment(UUID.randomUUID(), invoice.getTotalAmount(), LocalDateTime.now(), PaymentMethod.MOBILE_PAYMENT, PaymentStatus.COMPLETED));

        assertEquals(0, sampleHostel.getRevenue().compareTo(sampleManager.managePayments()));
    }

    // ==========================================
    // 7. TESTS SUR LES SERVICES (PACKAGE SERVICE)
    // ==========================================
    @Test
    public void testServiceLifecycle() {
        var laundry = new Service("Blanchisserie", "Nettoyage des vêtements", new BigDecimal("15.00"));
        assertTrue(laundry.isActive());

        laundry.cancel();
        assertFalse(laundry.isActive());

        laundry.activate();
        assertTrue(laundry.isActive());

        laundry.setName("Pressing");
        laundry.setDescription("Nettoyage et repassage");
        laundry.setPrice(new BigDecimal("20.00"));
        assertEquals("Pressing", laundry.getName());
        assertEquals("Nettoyage et repassage", laundry.getDescription());
        assertEquals(0, new BigDecimal("20.00").compareTo(laundry.getPrice()));
    }

    @Test
    public void testServiceOrderLifecycle() {
        var laundry = new Service("Blanchisserie", "Nettoyage des vêtements", new BigDecimal("15.00"));
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);

        var order = new ServiceOrder(reservation, laundry, 2);
        assertEquals(0, new BigDecimal("30.00").compareTo(order.getTotalPrice()));
        assertEquals(ServiceOrderStatus.PENDING, order.getStatus());
        assertTrue(order.isAvailable());

        order.markOrdered();
        assertEquals(ServiceOrderStatus.ORDERED, order.getStatus());
        assertFalse(order.isAvailable());

        order.markDelivered();
        assertEquals(ServiceOrderStatus.DELIVERED, order.getStatus());

        order.cancel();
        assertEquals(ServiceOrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    public void testServiceReservationManagement() {
        var srm = new ServiceReservationManagement();
        var wifi = new Service("Premium Wifi", "Haut débit", new BigDecimal("5.00"));
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);

        var order = srm.placeOrder(reservation, wifi, 3);
        assertNotNull(order);
        assertEquals(1, srm.getOrders().size());
        assertEquals(order, srm.findOrder(order.getId()));

        // Calcul total pour la réservation
        assertEquals(0, new BigDecimal("15.00").compareTo(srm.getOrdersTotalForReservation(reservation.getId())));

        // Alternative : commander un service inactif
        wifi.cancel(); // Désactive le service
        assertThrows(ServiceOrderException.class, () -> {
            srm.placeOrder(reservation, wifi, 1);
        });

        // Annulation de commande
        wifi.activate();
        var order2 = srm.placeOrder(reservation, wifi, 1);
        srm.cancelOrder(order2.getId());
        assertEquals(ServiceOrderStatus.CANCELLED, order2.getStatus());
    }

    @Test
    public void testServiceReservationManagementAlternatives() {
        var srm = new ServiceReservationManagement();
        var wifi = new Service("Premium Wifi", "Haut débit", new BigDecimal("5.00"));
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);

        // Aucune commande -> total ZERO, pas d'exception
        assertEquals(0, BigDecimal.ZERO.compareTo(srm.getOrdersTotalForReservation(reservation.getId())));

        // Commande introuvable
        assertNull(srm.findOrder(UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> srm.cancelOrder(UUID.randomUUID()));

        // Contrôles de nullité
        assertThrows(NullPointerException.class, () -> srm.placeOrder(null, wifi, 1));
        assertThrows(NullPointerException.class, () -> srm.placeOrder(reservation, null, 1));

        // getOrders() est non-modifiable
        var order = srm.placeOrder(reservation, wifi, 1);
        assertThrows(UnsupportedOperationException.class, () -> srm.getOrders().add(order));
    }

    @Test
    public void testServiceRoomManagement() {
        var srm = new ServiceRoomManagement();

        srm.markMaintenance(sampleRoom);
        assertEquals(RoomStatus.MAINTENANCE, sampleRoom.getStatus());

        srm.markOutOfOrder(sampleRoom);
        assertEquals(RoomStatus.OUT_OF_ORDER, sampleRoom.getStatus());

        srm.markAvailable(sampleRoom);
        assertEquals(RoomStatus.AVAILABLE, sampleRoom.getStatus());

        assertThrows(NullPointerException.class, () -> srm.markAvailable(null));
        assertThrows(NullPointerException.class, () -> srm.markMaintenance(null));
        assertThrows(NullPointerException.class, () -> srm.markOutOfOrder(null));
    }

    @Test
    public void testServiceRoomManagementListAvailableRoomsDelegatesToHostel() {
        var srm = new ServiceRoomManagement();
        var available = srm.listAvailableRooms(sampleHostel, RoomType.SINGLE, sampleDateRange);
        assertEquals(1, available.size());
        assertEquals(sampleRoom, available.get(0));

        assertThrows(NullPointerException.class, () -> srm.listAvailableRooms(null, RoomType.SINGLE, sampleDateRange));
    }

    @Test
    public void testServiceNotification() {
        var notifier = new ServiceNotification();
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);

        var createdMsg = notifier.notifyReservationCreated(reservation);
        assertTrue(createdMsg.contains(reservation.getCode()));
        assertTrue(createdMsg.contains(sampleCustomer.getFullName()));

        var cancelledMsg = notifier.notifyReservationCancelled(reservation);
        assertTrue(cancelledMsg.contains(reservation.getCode()));

        var invoice = new Invoice(reservation, reservation.getTotalPrice());
        var paymentMsg = notifier.notifyPayment(invoice);
        assertTrue(paymentMsg.contains(invoice.getInvoiceNumber()));

        assertEquals(3, notifier.getMessages().size());
        assertThrows(UnsupportedOperationException.class, () -> notifier.getMessages().add("hack"));
    }

    // ==========================================
    // 8. TESTS SUR LES EXCEPTIONS METIER
    // ==========================================
    @Test
    public void testExceptionsStructure() {
        var paymentId = UUID.randomUUID();
        var paymentEx = new PaymentException(paymentId, "Solde insuffisant");
        assertEquals(paymentId, paymentEx.getPaymentId());
        assertEquals("Solde insuffisant", paymentEx.getReason());
        assertEquals("Solde insuffisant", paymentEx.getMessage());

        var idrEx = new InvalidDateRangeException(sampleDateRange);
        assertEquals(sampleDateRange, idrEx.getDateRange());
        assertNotNull(idrEx.getMessage());

        var rnaEx = new RoomNotAvailableException(RoomType.SUITE, sampleDateRange);
        assertEquals(RoomType.SUITE, rnaEx.getRoomType());
        assertEquals(sampleDateRange, rnaEx.getDateRange());
    }

    @Test
    public void testExceptionsAlternativeMessageOnlyConstructors() {
        // Constructeurs "message seul" : toujours utilisables, mais sans contexte structuré
        var idrEx = new InvalidDateRangeException("Plage de dates invalide pour une raison métier");
        assertEquals("Plage de dates invalide pour une raison métier", idrEx.getMessage());
        assertNull(idrEx.getDateRange());

        var rnaEx = new RoomNotAvailableException("Chambre indisponible");
        assertEquals("Chambre indisponible", rnaEx.getMessage());
        assertNull(rnaEx.getRoomType());
        assertNull(rnaEx.getDateRange());

        var soEx = new ServiceOrderException("Service inactif");
        assertEquals("Service inactif", soEx.getMessage());

        // Toutes héritent bien de BusinessException (donc de RuntimeException)
        assertTrue(idrEx instanceof BusinessException);
        assertTrue(rnaEx instanceof BusinessException);
        assertTrue(soEx instanceof BusinessException);
        assertTrue(paymentExceptionIsBusinessException());
    }

    private boolean paymentExceptionIsBusinessException() {
        return new PaymentException(UUID.randomUUID(), "x") instanceof BusinessException;
    }

    // ==========================================
    // 9. TESTS SUR SCHEDULING
    // ==========================================
    @Test
    public void testSchedulingIsAvailableHonoursRoomStatus() {
        var scheduling = new Scheduling();
        assertTrue(scheduling.isAvailable(sampleRoom, sampleDateRange));

        sampleRoom.setStatus(RoomStatus.MAINTENANCE);
        assertFalse(scheduling.isAvailable(sampleRoom, sampleDateRange));
    }

    @Test
    public void testSchedulingAddReservationSuccessAndConflict() {
        var scheduling = new Scheduling();
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        scheduling.addReservation(reservation);
        assertEquals(1, scheduling.getReservations().size());

        // Alternative : une deuxième réservation qui chevauche la première sur la même chambre
        var overlapping = new Reservation(sampleCustomer, sampleRoom,
                new DateRange(sampleDateRange.getStartDate().plusDays(1), sampleDateRange.getEndDate().plusDays(1)));
        assertThrows(RoomNotAvailableException.class, () -> scheduling.addReservation(overlapping));

        assertThrows(NullPointerException.class, () -> scheduling.addReservation(null));
    }

    @Test
    public void testSchedulingCancelledReservationDoesNotBlockAvailability() {
        var scheduling = new Scheduling();
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        scheduling.addReservation(reservation);
        reservation.cancel();

        // Comme la réservation est annulée (isActive() == false), elle ne doit plus bloquer la chambre
        assertTrue(scheduling.isAvailable(sampleRoom, sampleDateRange));
    }

    @Test
    public void testSchedulingBackToBackDoesNotBlockAvailability() {
        var scheduling = new Scheduling();
        var first = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        scheduling.addReservation(first);

        var backToBack = new DateRange(sampleDateRange.getEndDate(), sampleDateRange.getEndDate().plusDays(2));
        assertTrue(scheduling.isAvailable(sampleRoom, backToBack));
    }

    @Test
    public void testSchedulingRemoveReservation() {
        var scheduling = new Scheduling();
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        scheduling.addReservation(reservation);

        scheduling.removeReservation(reservation.getId());
        assertTrue(scheduling.getReservations().isEmpty());

        // Alternative : suppression d'un id inconnu -> ne fait rien, pas d'exception
        assertDoesNotThrow(() -> scheduling.removeReservation(UUID.randomUUID()));
    }

    @Test
    public void testSchedulingGetReservationsForRoomAndDateRange() {
        var scheduling = new Scheduling();
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        scheduling.addReservation(reservation);

        var matches = scheduling.getReservations(sampleRoom, sampleDateRange);
        assertEquals(1, matches.size());

        var noMatches = scheduling.getReservations(sampleRoom,
                new DateRange(sampleDateRange.getEndDate().plusDays(5), sampleDateRange.getEndDate().plusDays(6)));
        assertTrue(noMatches.isEmpty());
    }

    // ==========================================
    // 10. TESTS SUR STAY
    // ==========================================
    @Test
    public void testStayLifecycle() {
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        var stay = new Stay(reservation);

        assertEquals(reservation, stay.getReservation());
        assertEquals(sampleDateRange.getStartDate().atStartOfDay(), stay.getCheckIn());
        assertEquals(sampleDateRange.getEndDate().atStartOfDay(), stay.getCheckOut());
        assertFalse(stay.isCurrentlyStaying());

        stay.checkIn();
        assertNotNull(stay.getActualCheckIn());
        assertTrue(stay.isCurrentlyStaying());

        stay.checkOut();
        assertNotNull(stay.getActualCheckOut());
        assertFalse(stay.isCurrentlyStaying());
    }

    @Test
    public void testStayConstructorRejectsNullReservation() {
        assertThrows(NullPointerException.class, () -> new Stay(null));
    }

    // ==========================================
    // 11. TESTS SUR RESERVATIONHISTORY
    // ==========================================
    @Test
    public void testReservationHistoryConvenienceConstructor() {
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        var now = LocalDateTime.now();
        var history = new ReservationHistory(reservation, "CREATED", now, "Réservation créée");

        assertNotNull(history.getId());
        assertEquals(reservation, history.getReservation());
        assertEquals("CREATED", history.getAction());
        assertEquals(now, history.getActionDate());
        assertEquals("Réservation créée", history.getNotes());
    }

    @Test
    public void testHostelTracksFullReservationHistoryTimeline() {
        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        sampleHostel.checkIn(reservation.getId());
        sampleHostel.checkOut(reservation.getId());
        var invoice = sampleHostel.generateInvoice(reservation.getId());
        sampleHostel.payInvoice(invoice.getId(),
                new Payment(UUID.randomUUID(), invoice.getTotalAmount(), LocalDateTime.now(), PaymentMethod.BANK_TRANSFER, PaymentStatus.COMPLETED));

        var actions = sampleHostel.getReservationHistory().stream().map(ReservationHistory::getAction).toList();
        assertTrue(actions.contains("CREATED"));
        assertTrue(actions.contains("CHECK_IN"));
        assertTrue(actions.contains("CHECK_OUT"));
        assertTrue(actions.contains("INVOICE_GENERATED"));
        assertTrue(actions.contains("PAYMENT_RECEIVED"));
    }

    // ==========================================
    // 12. TESTS SUR PAYMENT
    // ==========================================
    @Test
    public void testPaymentIsSuccessfulOnlyWhenCompleted() {
        var completed = new Payment(UUID.randomUUID(), new BigDecimal("50.00"), LocalDateTime.now(), PaymentMethod.CASH, PaymentStatus.COMPLETED);
        var pending = new Payment(UUID.randomUUID(), new BigDecimal("50.00"), LocalDateTime.now(), PaymentMethod.CASH, PaymentStatus.PENDING);
        var failed = new Payment(UUID.randomUUID(), new BigDecimal("50.00"), LocalDateTime.now(), PaymentMethod.CASH, PaymentStatus.FAILED);
        var refunded = new Payment(UUID.randomUUID(), new BigDecimal("50.00"), LocalDateTime.now(), PaymentMethod.CASH, PaymentStatus.REFUNDED);

        assertTrue(completed.isSuccessful());
        assertFalse(pending.isSuccessful());
        assertFalse(failed.isSuccessful());
        assertFalse(refunded.isSuccessful());
    }

    @Test
    public void testPaymentNoArgsConstructorAndSetters() {
        var payment = new Payment();
        payment.setAmount(new BigDecimal("42.00"));
        payment.setMethod(PaymentMethod.DEBIT_CARD);
        payment.setStatus(PaymentStatus.COMPLETED);

        assertEquals(0, new BigDecimal("42.00").compareTo(payment.getAmount()));
        assertEquals(PaymentMethod.DEBIT_CARD, payment.getMethod());
        assertTrue(payment.isSuccessful());
    }

    // ==========================================
    // 13. TESTS SUR PRICING
    // ==========================================
    @Test
    public void testPricingIsValidAndGetPriceFor() {
        var pricing = new Pricing(UUID.randomUUID(), RoomType.SUITE, new BigDecimal("200.00"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31));

        assertTrue(pricing.isValid(LocalDate.of(2026, 7, 15)));
        assertTrue(pricing.isValid(LocalDate.of(2026, 6, 1)));  // borne basse incluse
        assertTrue(pricing.isValid(LocalDate.of(2026, 8, 31))); // borne haute incluse
        assertFalse(pricing.isValid(LocalDate.of(2026, 5, 31)));
        assertFalse(pricing.isValid(LocalDate.of(2026, 9, 1)));
        assertFalse(pricing.isValid(null));

        assertEquals(0, new BigDecimal("200.00").compareTo(pricing.getPriceFor(LocalDate.of(2026, 7, 15))));
        assertEquals(0, BigDecimal.ZERO.compareTo(pricing.getPriceFor(LocalDate.of(2026, 9, 1))));
    }

    @Test
    public void testPricingWithMissingValidityWindowIsAlwaysInvalid() {
        var pricing = new Pricing();
        pricing.setBasePrice(new BigDecimal("100.00"));
        // validFrom / validTo jamais renseignés
        assertFalse(pricing.isValid(LocalDate.now()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pricing.getPriceFor(LocalDate.now())));
    }

    // ==========================================
    // 14. TESTS SUR HOSTEL - ETAGES & CHAMBRES
    // ==========================================
    @Test
    public void testHostelConstructorNullChecks() {
        assertThrows(NullPointerException.class, () -> new Hostel(null, sampleAddress, "0340000000", "a@b.com"));
        assertThrows(NullPointerException.class, () -> new Hostel("Nom", null, "0340000000", "a@b.com"));
        assertThrows(NullPointerException.class, () -> new Hostel("Nom", sampleAddress, null, "a@b.com"));
        assertThrows(NullPointerException.class, () -> new Hostel("Nom", sampleAddress, "0340000000", null));
    }

    @Test
    public void testHostelAddAndRemoveFloor() {
        var floor2 = new Floor(2);
        sampleHostel.addFloor(floor2);
        assertEquals(floor2, sampleHostel.getFloor(2));

        // Alternative : étage déjà existant
        assertThrows(IllegalArgumentException.class, () -> sampleHostel.addFloor(new Floor(2)));
        assertThrows(NullPointerException.class, () -> sampleHostel.addFloor(null));

        var removed = sampleHostel.removeFloor(floor2.getId());
        assertEquals(floor2, removed);
        assertNull(sampleHostel.getFloor(2));

        // Alternative : étage déjà supprimé / inconnu -> null, pas d'exception
        assertNull(sampleHostel.removeFloor(floor2.getId()));
        assertThrows(NullPointerException.class, () -> sampleHostel.removeFloor(null));
    }

    @Test
    public void testHostelAddRoomToUnknownFloorFails() {
        assertThrows(IllegalArgumentException.class,
                () -> sampleHostel.addRoom(99, new Room("999", RoomType.SINGLE, new BigDecimal("10.00"))));
    }

    @Test
    public void testHostelFindRoomAlternatives() {
        assertEquals(sampleRoom, sampleHostel.findRoom("101"));
        assertNull(sampleHostel.findRoom("999"));

        assertEquals(sampleRoom, sampleHostel.findRoomById(sampleRoom.getId()));
        assertNull(sampleHostel.findRoomById(UUID.randomUUID()));
    }

    @Test
    public void testHostelGetAllRoomsAggregatesAcrossFloors() {
        var floor2 = new Floor(2);
        sampleHostel.addFloor(floor2);
        var room2 = new Room("201", RoomType.SUITE, new BigDecimal("200.00"));
        sampleHostel.addRoom(2, room2);

        var allRooms = sampleHostel.getAllRooms();
        assertEquals(2, allRooms.size());
        assertTrue(allRooms.contains(sampleRoom));
        assertTrue(allRooms.contains(room2));
        assertThrows(UnsupportedOperationException.class, () -> allRooms.add(room2));
    }

    @Test
    public void testHostelGetAvailableRoomsFiltersByTypeAndAvailability() {
        var room2 = new Room("102", RoomType.SINGLE, new BigDecimal("80.00"));
        sampleHostel.addRoom(1, room2);

        // Les deux chambres SINGLE sont libres au départ
        assertEquals(2, sampleHostel.getAvailableRooms(RoomType.SINGLE, sampleDateRange).size());

        // On réserve sampleRoom -> il ne doit plus apparaître pour ces dates
        sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        var available = sampleHostel.getAvailableRooms(RoomType.SINGLE, sampleDateRange);
        assertEquals(1, available.size());
        assertEquals(room2, available.get(0));

        // Aucune chambre DOUBLE n'existe encore
        assertTrue(sampleHostel.getAvailableRooms(RoomType.DOUBLE, sampleDateRange).isEmpty());
    }

    // ==========================================
    // 15. TESTS SUR HOSTEL - CYCLE DE VIE COMPLET DES RESERVATIONS
    // ==========================================
    @Test
    public void testHostelCreateReservationByRoomNumberAlternatives() {
        assertThrows(IllegalArgumentException.class,
                () -> sampleHostel.createReservation(sampleCustomer, "INCONNU", sampleDateRange));

        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(1, sampleHostel.getReservations().size());
    }

    @Test
    public void testHostelCreateReservationByRoomIdAlternatives() {
        assertThrows(IllegalArgumentException.class,
                () -> sampleHostel.createReservation(sampleCustomer, UUID.randomUUID(), sampleDateRange));

        var reservation = sampleHostel.createReservation(sampleCustomer, sampleRoom.getId(), sampleDateRange);
        assertEquals(sampleRoom, reservation.getRoom());
    }

    @Test
    public void testHostelCreateReservationByRoomTypeAlternatives() {
        var reservation = sampleHostel.createReservation(sampleCustomer, RoomType.SINGLE, sampleDateRange);
        assertEquals(sampleRoom, reservation.getRoom());

        // Aucune chambre DOUBLE disponible
        assertThrows(RoomNotAvailableException.class,
                () -> sampleHostel.createReservation(sampleCustomer, RoomType.DOUBLE, sampleDateRange));
    }

    @Test
    public void testHostelCreateReservationRejectsPastDates() {
        var pastRange = new DateRange(LocalDate.now().minusDays(5), LocalDate.now().minusDays(2));
        var ex = assertThrows(InvalidDateRangeException.class,
                () -> sampleHostel.createReservation(sampleCustomer, "101", pastRange));
        assertEquals(pastRange, ex.getDateRange());
    }

    @Test
    public void testHostelCreateReservationRejectsUnavailableRoomStatus() {
        sampleRoom.setStatus(RoomStatus.MAINTENANCE);
        assertThrows(RoomNotAvailableException.class,
                () -> sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange));
    }

    @Test
    public void testHostelCreateReservationRejectsOverlap() {
        sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);

        var overlapping = new DateRange(sampleDateRange.getStartDate().plusDays(1), sampleDateRange.getEndDate().plusDays(1));
        var ex = assertThrows(RoomNotAvailableException.class,
                () -> sampleHostel.createReservation(sampleCustomer, "101", overlapping));
        assertEquals(RoomType.SINGLE, ex.getRoomType());
    }

    @Test
    public void testHostelAllowsBackToBackReservations() {
        // Régression : un départ et une arrivée le même jour ne doivent plus être bloqués
        var first = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        var backToBack = new DateRange(sampleDateRange.getEndDate(), sampleDateRange.getEndDate().plusDays(2));

        var second = assertDoesNotThrow(() -> sampleHostel.createReservation(sampleCustomer, "101", backToBack));
        assertNotEquals(first.getId(), second.getId());
        assertEquals(2, sampleHostel.getReservations().size());
    }

    @Test
    public void testHostelCancelReservationAlternatives() {
        assertThrows(IllegalArgumentException.class, () -> sampleHostel.cancelReservation(UUID.randomUUID()));

        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        sampleHostel.cancelReservation(reservation.getId());
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());

        // La chambre redevient disponible pour les mêmes dates une fois annulée
        assertDoesNotThrow(() -> sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange));
    }

    @Test
    public void testHostelCannotCancelWhileGuestIsCurrentlyStaying() {
        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        sampleHostel.checkIn(reservation.getId());

        assertThrows(IllegalStateException.class, () -> sampleHostel.cancelReservation(reservation.getId()));
    }

    @Test
    public void testHostelCheckInAlternatives() {
        assertThrows(IllegalArgumentException.class, () -> sampleHostel.checkIn(UUID.randomUUID()));

        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);
        sampleHostel.cancelReservation(reservation.getId());
        assertThrows(IllegalStateException.class, () -> sampleHostel.checkIn(reservation.getId()));

        var reservation2 = sampleHostel.createReservation(sampleCustomer, "101",
                new DateRange(sampleDateRange.getEndDate(), sampleDateRange.getEndDate().plusDays(1)));
        var stay = sampleHostel.checkIn(reservation2.getId());
        assertTrue(stay.isCurrentlyStaying());
        assertTrue(sampleHostel.getStays().contains(stay));

        // Alternative : double check-in sur la même réservation
        assertThrows(IllegalStateException.class, () -> sampleHostel.checkIn(reservation2.getId()));
    }

    @Test
    public void testHostelCheckOutAlternatives() {
        var reservation = sampleHostel.createReservation(sampleCustomer, "101", sampleDateRange);

        // Pas encore check-in -> aucun séjour actif
        assertThrows(IllegalArgumentException.class, () -> sampleHostel.checkOut(reservation.getId()));

        sampleHostel.checkIn(reservation.getId());
        sampleHostel.checkOut(reservation.getId());

        // Alternative : checkout une seconde fois sur la même réservation
        assertThrows(IllegalArgumentException.class, () -> sampleHostel.checkOut(reservation.getId()));
    }

    @Test
    public void testHostelGetReservationReturnsNullWhenNotFound() {
        assertNull(sampleHostel.getReservation(UUID.randomUUID()));
    }
}