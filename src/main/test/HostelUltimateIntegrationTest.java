

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
    }

    @Test
    public void testDateRangeOverlaps() {
        var range1 = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5));
        var range2 = new DateRange(LocalDate.of(2026, 7, 4), LocalDate.of(2026, 7, 10));
        var range3 = new DateRange(LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 10));

        assertTrue(range1.overlaps(range2));
        assertFalse(range1.overlaps(range3)); // Turnover exclusif (le jour même du checkout)
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
    public void testRoomStatusAndPrice() {
        sampleRoom.setPricePerNight(new BigDecimal("90.00"));
        assertEquals(0, new BigDecimal("90.00").compareTo(sampleRoom.getPricePerNight()));

        assertTrue(sampleRoom.isAvailable());
        assertTrue(sampleRoom.isOperational());

        sampleRoom.setStatus(RoomStatus.OUT_OF_ORDER);
        assertFalse(sampleRoom.isAvailable());
        assertFalse(sampleRoom.isOperational());
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

    // ==========================================
    // 5. TESTS SUR INVOICE
    // ==========================================
    @Test
    public void testInvoiceAndRevenue() {
        var reservation = new Reservation(sampleCustomer, sampleRoom, sampleDateRange);
        var invoice = new Invoice(reservation, reservation.getTotalPrice());

        assertNotNull(invoice.getId());
        assertNotNull(invoice.getInvoiceNumber());
        assertEquals(InvoiceStatus.UNPAID, invoice.getStatus());

        invoice.markAsPaid();
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
    }

    // ==========================================
    // 6. TESTS SUR LE MANAGER
    // ==========================================
    @Test
    public void testManagerActionsWithoutHostel() {
        var standaloneManager = new Manager("Bob", "Sponge", "bob@email.com", "123", "EMP-002");
        assertThrows(IllegalStateException.class, () -> standaloneManager.addRoom(1, sampleRoom));
    }

    @Test
    public void testManagerGenerateReports() {
        var report = sampleManager.generateReports();
        assertTrue(report.contains("Le Grand Hostel"));
        assertTrue(report.contains("Revenue:"));
    }

    // ==========================================
    // 7. TESTS SUR LES SERVICES (PACKAGES SERVICE)
    // ==========================================
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
    public void testServiceRoomManagement() {
        var srm = new ServiceRoomManagement();

        srm.markMaintenance(sampleRoom);
        assertEquals(RoomStatus.MAINTENANCE, sampleRoom.getStatus());

        srm.markOutOfOrder(sampleRoom);
        assertEquals(RoomStatus.OUT_OF_ORDER, sampleRoom.getStatus());

        srm.markAvailable(sampleRoom);
        assertEquals(RoomStatus.AVAILABLE, sampleRoom.getStatus());
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

        var idrEx = new InvalidDateRangeException(sampleDateRange);
        assertEquals(sampleDateRange, idrEx.getDateRange());
        assertNotNull(idrEx.getMessage());

        var rnaEx = new RoomNotAvailableException(RoomType.SUITE, sampleDateRange);
        assertEquals(RoomType.SUITE, rnaEx.getRoomType());
        assertEquals(sampleDateRange, rnaEx.getDateRange());
    }
}