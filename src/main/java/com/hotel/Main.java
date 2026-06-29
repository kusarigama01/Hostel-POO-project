package com.hotel;

import com.hotel.model.*;
import com.hotel.service.ServiceNotification;
import com.hotel.service.ServiceReservationManagement;
import com.hotel.service.ServiceRoomManagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Hostel hostel = bootstrapHostel();
        Customer customer = new Customer(
                "Tojotiana",
                "Jakoba",
                "guest@example.com",
                "+261 34 12 345 67",
                LocalDate.of(2000, 1, 1),
                new Address("Talatamaty", "Analamanga", "Ambohidratrimo", "101", "Madagascar")
        );

        ServiceReservationManagement serviceReservationManagement = new ServiceReservationManagement();
        ServiceNotification notificationService = new ServiceNotification();
        ServiceRoomManagement roomManagement = new ServiceRoomManagement();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== MAKIREALM HOSTEL ===");
                System.out.println("1. List rooms");
                System.out.println("2. List available rooms");
                System.out.println("3. Create reservation");
                System.out.println("4. Cancel reservation");
                System.out.println("5. Check-in");
                System.out.println("6. Check-out");
                System.out.println("7. Create service order");
                System.out.println("8. Generate invoice and pay");
                System.out.println("9. Mark room maintenance");
                System.out.println("0. Exit");
                System.out.print("Choice: ");

                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> hostel.getAllRooms().forEach(System.out::println);
                        case "2" -> listAvailableRooms(scanner, hostel);
                        case "3" -> {
                            Reservation reservation = createReservationInteractive(scanner, hostel, customer, notificationService);
                            System.out.println("Created: " + reservation.getCode() + " total=" + reservation.getTotalPrice());
                        }
                        case "4" -> cancelReservationInteractive(scanner, hostel, notificationService);
                        case "5" -> checkInInteractive(scanner, hostel);
                        case "6" -> checkOutInteractive(scanner, hostel);
                        case "7" -> createServiceOrderInteractive(scanner, hostel, serviceReservationManagement);
                        case "8" -> generateAndPayInteractive(scanner, hostel, notificationService);
                        case "9" -> markRoomMaintenanceInteractive(scanner, hostel, roomManagement);
                        case "0" -> {
                            System.out.println("Goodbye.");
                            return;
                        }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private static Hostel bootstrapHostel() {
        Hostel hostel = new Hostel(
                "Makirealm Hostel",
                new Address("10 Avenue", "Antananarivo", "Analamanga", "101", "Madagascar"),
                "+261 32 71 816 87",
                "contact@makirealm.local"
        );
        hostel.addFloor(new Floor(1));
        hostel.addFloor(new Floor(2));
        hostel.addRoom(1, new Room("101", RoomType.SINGLE, BigDecimal.valueOf(50)));
        hostel.addRoom(1, new Room("102", RoomType.DOUBLE, BigDecimal.valueOf(80)));
        hostel.addRoom(1, new Room("103", RoomType.DOUBLE, BigDecimal.valueOf(80)));
        hostel.addRoom(2, new Room("201", RoomType.SUITE, BigDecimal.valueOf(150)));
        hostel.addRoom(2, new Room("202", RoomType.SUITE, BigDecimal.valueOf(150)));
        hostel.addRoom(2, new Room("301", RoomType.DELUXE, BigDecimal.valueOf(150)));
        return hostel;
    }

    private static void listAvailableRooms(Scanner scanner, Hostel hostel) {
        RoomType type = askRoomType(scanner);
        DateRange range = askDateRange(scanner);
        List<Room> rooms = hostel.getAvailableRooms(type, range);
        if (rooms.isEmpty()) {
            System.out.println("No available rooms.");
        } else {
            rooms.forEach(System.out::println);
        }
    }

    private static Reservation createReservationInteractive(Scanner scanner, Hostel hostel, Customer customer, ServiceNotification notificationService) {
        System.out.print("Room number: ");
        String roomNumber = scanner.nextLine().trim();
        DateRange range = askDateRange(scanner);
        Reservation reservation = hostel.createReservation(customer, roomNumber, range);
        notificationService.notifyReservationCreated(reservation);
        return reservation;
    }

    private static void cancelReservationInteractive(Scanner scanner, Hostel hostel, ServiceNotification notificationService) {
        Reservation reservation = askReservationByCode(scanner, hostel);
        hostel.cancelReservation(reservation.getId());
        notificationService.notifyReservationCancelled(reservation);
        System.out.println("Cancelled.");
    }

    private static void checkInInteractive(Scanner scanner, Hostel hostel) {
        Reservation reservation = askReservationByCode(scanner, hostel);
        Stay stay = hostel.checkIn(reservation.getId());
        System.out.println("Check-in at " + stay.getActualCheckIn());
    }

    private static void checkOutInteractive(Scanner scanner, Hostel hostel) {
        Reservation reservation = askReservationByCode(scanner, hostel);
        hostel.checkOut(reservation.getId());
        System.out.println("Checked out.");
    }

    private static void createServiceOrderInteractive(Scanner scanner, Hostel hostel, ServiceReservationManagement management) {
        Reservation reservation = askReservationByCode(scanner, hostel);
        System.out.print("Service name: ");
        String serviceName = scanner.nextLine().trim();
        System.out.print("Service description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Service price: ");
        BigDecimal price = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());

        Service service = new Service(serviceName, description, price);
        ServiceOrder order = management.placeOrder(reservation, service, quantity);
        System.out.println("Order placed: " + order.getTotalPrice());
    }

    private static void generateAndPayInteractive(Scanner scanner, Hostel hostel, ServiceNotification notificationService) {
        Reservation reservation = askReservationByCode(scanner, hostel);
        Invoice invoice = hostel.generateInvoice(reservation.getId());
        System.out.println("Invoice: " + invoice.getInvoiceNumber() + " amount=" + invoice.getTotalAmount());
        PaymentMethod method = askPaymentMethod(scanner);
        System.out.print("Payment amount: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

        Payment payment = new Payment(invoice, amount, method);
        payment.process();
        notificationService.notifyPayment(invoice);
        System.out.println("Payment status: " + payment.getStatus());
    }

    private static void markRoomMaintenanceInteractive(Scanner scanner, Hostel hostel, ServiceRoomManagement roomManagement) {
        System.out.print("Room number: ");
        String roomNumber = scanner.nextLine().trim();
        Room room = hostel.findRoom(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }
        roomManagement.markMaintenance(room);
        System.out.println("Room marked as maintenance.");
    }

    private static Reservation askReservationByCode(Scanner scanner, Hostel hostel) {
        System.out.print("Reservation code: ");
        String code = scanner.nextLine().trim();
        return hostel.getReservations().stream()
                .filter(r -> r.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
    }

    private static RoomType askRoomType(Scanner scanner) {
        System.out.print("Room type (SINGLE, DOUBLE, TWIN, SUITE, DELUXE): ");
        return RoomType.valueOf(scanner.nextLine().trim().toUpperCase());
    }

    private static DateRange askDateRange(Scanner scanner) {
        System.out.print("Start date (YYYY-MM-DD): ");
        LocalDate start = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("End date (YYYY-MM-DD): ");
        LocalDate end = LocalDate.parse(scanner.nextLine().trim());
        return new DateRange(start, end);
    }

    private static PaymentMethod askPaymentMethod(Scanner scanner) {
        System.out.print("Payment method (CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, MOBILE_PAYMENT): ");
        return PaymentMethod.valueOf(scanner.nextLine().trim().toUpperCase());
    }
}
