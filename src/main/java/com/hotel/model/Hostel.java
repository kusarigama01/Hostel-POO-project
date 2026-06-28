package com.hotel.model;

import com.hotel.exception.InvalidDateRangeException;
import com.hotel.exception.RoomNotAvailableException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Hostel {
    private final UUID id;
    private final String name;
    private final Address address;
    private final String phone;
    private final String email;
    private final List<Floor> floors;
    private final Scheduling scheduling;
    private final List<Reservation> reservations;
    private final List<ReservationHistory> reservationHistory;
    private final List<Invoice> invoices;
    private final List<Stay> stays;

    public Hostel(String name, Address address, String phone, String email) {
        this.id = UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.phone = Objects.requireNonNull(phone, "phone must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.floors = new ArrayList<>();
        this.scheduling = new Scheduling();
        this.reservations = new ArrayList<>();
        this.reservationHistory = new ArrayList<>();
        this.invoices = new ArrayList<>();
        this.stays = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public Scheduling getScheduling() {
        return scheduling;
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public List<ReservationHistory> getReservationHistory() {
        return Collections.unmodifiableList(reservationHistory);
    }

    public List<Invoice> getInvoices() {
        return Collections.unmodifiableList(invoices);
    }

    public List<Stay> getStays() {
        return Collections.unmodifiableList(stays);
    }

    public void addFloor(Floor floor) {
        Objects.requireNonNull(floor, "floor must not be null");
        if (getFloor(floor.getNumber()) != null) {
            throw new IllegalArgumentException("Floor already exists");
        }
        floors.add(floor);
    }

    // ADDED: matches the UML's removeFloor(floorId: UUID): void, which had no
    // implementation at all.
    public Floor removeFloor(UUID floorId) {
        Objects.requireNonNull(floorId, "floorId must not be null");
        Floor target = null;
        for (Floor floor : floors) {
            if (floor.getId().equals(floorId)) {
                target = floor;
                break;
            }
        }
        if (target != null) {
            floors.remove(target);
        }
        return target;
    }

    public Floor getFloor(int floorNumber) {
        for (Floor floor : floors) {
            if (floor.getNumber() == floorNumber) {
                return floor;
            }
        }
        return null;
    }

    public void addRoom(int floorNumber, Room room) {
        Floor floor = getFloor(floorNumber);
        if (floor == null) {
            throw new IllegalArgumentException("Floor " + floorNumber + " not found");
        }
        floor.addRoom(room);
    }

    public Room findRoom(String roomNumber) {
        for (Floor floor : floors) {
            Room room = floor.getRoomByNumber(roomNumber);
            if (room != null) {
                return room;
            }
        }
        return null;
    }

    public Room findRoomById(UUID roomId) {
        for (Floor floor : floors) {
            for (Room room : floor.getRooms()) {
                if (room.getId().equals(roomId)) {
                    return room;
                }
            }
        }
        return null;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        for (Floor floor : floors) {
            rooms.addAll(floor.getRooms());
        }
        return Collections.unmodifiableList(rooms);
    }

    public List<Room> getAvailableRooms(RoomType roomType, DateRange dateRange) {
        List<Room> availableRooms = new ArrayList<>();
        for (Room room : getAllRooms()) {
            if (room.getRoomType() == roomType && scheduling.isAvailable(room, dateRange)) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    public Reservation createReservation(Customer customer, String roomNumber, DateRange dateRange) {
        Room room = findRoom(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomNumber);
        }
        return createReservation(customer, room, dateRange);
    }

    // ADDED: overload matching the UML's createReservation(customer, roomId: UUID, dateRange)
    public Reservation createReservation(Customer customer, UUID roomId, DateRange dateRange) {
        Room room = findRoomById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        return createReservation(customer, room, dateRange);
    }

    public Reservation createReservation(Customer customer, RoomType roomType, DateRange dateRange) {
        Room room = getAvailableRooms(roomType, dateRange).stream()
                .findFirst()
                .orElseThrow(() -> new RoomNotAvailableException(roomType, dateRange));
        return createReservation(customer, room, dateRange);
    }

    public Reservation createReservation(Customer customer, Room room, DateRange dateRange) {
        Objects.requireNonNull(customer, "customer must not be null");
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(dateRange, "dateRange must not be null");

        // ADDED: reject reservations that start in the past. This is the one
        // place InvalidDateRangeException actually makes sense to use — the
        // dates are structurally fine (DateRange already enforces start <= end),
        // it's a business rule, not a malformed value.
        if (dateRange.getStartDate().isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException(dateRange);
        }

        if (!scheduling.isAvailable(room, dateRange)) {
            throw new RoomNotAvailableException(room.getRoomType(), dateRange);
        }

        Reservation reservation = new Reservation(customer, room, dateRange);
        reservation.confirm();
        reservations.add(reservation);
        scheduling.addReservation(reservation);
        reservationHistory.add(new ReservationHistory(reservation, "CREATED", LocalDateTime.now(), "Reservation created"));
        return reservation;
    }

    public void cancelReservation(UUID reservationId) {
        Reservation reservation = getReservation(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found");
        }
        // ADDED: guard against cancelling a reservation while the guest is
        // physically checked in.
        boolean currentlyStaying = stays.stream()
                .anyMatch(s -> s.getReservation().getId().equals(reservationId) && s.isCurrentlyStaying());
        if (currentlyStaying) {
            throw new IllegalStateException("Cannot cancel reservation " + reservation.getCode() + " while the guest is currently staying");
        }
        reservation.cancel();
        scheduling.removeReservation(reservationId);
        reservationHistory.add(new ReservationHistory(reservation, "CANCELLED", LocalDateTime.now(), "Reservation cancelled"));
    }

    public Reservation getReservation(UUID reservationId) {
        for (Reservation reservation : reservations) {
            if (reservation.getId().equals(reservationId)) {
                return reservation;
            }
        }
        return null;
    }

    public Stay checkIn(UUID reservationId) {
        Reservation reservation = getReservation(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found");
        }
        if (!reservation.isActive()) {
            throw new IllegalStateException("Cannot check in a cancelled reservation");
        }
        // ADDED: guard against checking the same reservation in twice.
        boolean alreadyCheckedIn = stays.stream()
                .anyMatch(s -> s.getReservation().getId().equals(reservationId) && s.isCurrentlyStaying());
        if (alreadyCheckedIn) {
            throw new IllegalStateException("Reservation " + reservation.getCode() + " is already checked in");
        }
        Stay stay = new Stay(reservation);
        stay.checkIn();
        stays.add(stay);
        reservationHistory.add(new ReservationHistory(reservation, "CHECK_IN", LocalDateTime.now(), "Guest checked in"));
        return stay;
    }

    public void checkOut(UUID reservationId) {
        // CHANGED: also require isCurrentlyStaying(), so calling checkOut twice
        // (or on a reservation that was never checked in) fails clearly instead
        // of silently re-stamping actualCheckOut.
        Stay stay = stays.stream()
                .filter(s -> s.getReservation().getId().equals(reservationId) && s.isCurrentlyStaying())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active stay found for this reservation"));
        stay.checkOut();
        reservationHistory.add(new ReservationHistory(stay.getReservation(), "CHECK_OUT", LocalDateTime.now(), "Guest checked out"));
    }

    public Invoice generateInvoice(UUID reservationId) {
        Reservation reservation = getReservation(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found");
        }
        Invoice invoice = new Invoice(reservation, reservation.getTotalPrice());
        invoices.add(invoice);
        reservationHistory.add(new ReservationHistory(reservation, "INVOICE_GENERATED", LocalDateTime.now(), "Invoice " + invoice.getInvoiceNumber() + " generated"));
        return invoice;
    }

    public Invoice getInvoice(UUID invoiceId) {
        for (Invoice invoice : invoices) {
            if (invoice.getId().equals(invoiceId)) {
                return invoice;
            }
        }
        return null;
    }

    // ADDED: gives Payment an actual role in the flow (per the diagram's
    // Payment 1—1 Invoice link) instead of being a model nobody ever uses.
    public void payInvoice(UUID invoiceId, Payment payment) {
        Invoice invoice = getInvoice(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found");
        }
        Objects.requireNonNull(payment, "payment must not be null");
        if (payment.isSuccessful()) {
            invoice.markAsPaid();
            reservationHistory.add(new ReservationHistory(invoice.getReservation(), "PAYMENT_RECEIVED", LocalDateTime.now(),
                    "Invoice " + invoice.getInvoiceNumber() + " paid"));
        }
    }

    public BigDecimal getRevenue() {
        return invoices.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
