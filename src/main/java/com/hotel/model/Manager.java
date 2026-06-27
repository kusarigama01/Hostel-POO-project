package com.hotel.model;

import java.math.BigDecimal;
import java.util.List;

public class Manager extends User {
    private String employeeId;
    private Hostel hostel;

    public Manager(String firstName, String lastName, String email, String phone, String employeeId) {
        super(firstName, lastName, email, phone);
        this.employeeId = employeeId;
    }

    public Manager(String firstName, String lastName, String email, String phone, String employeeId, Hostel hostel) {
        this(firstName, lastName, email, phone, employeeId);
        this.hostel = hostel;
    }

    public String getEmployeeId() { return employeeId; }

    public Hostel getHostel() { return hostel; }

    public void setHostel(Hostel hostel) { this.hostel = hostel; }

    public void addRoom(int floorNumber, Room room) {
        requireHostel();
        hostel.addRoom(floorNumber, room);
    }

    public void updateRoomPrice(String roomNumber, BigDecimal newPrice) {
        requireHostel();
        Room room = hostel.findRoom(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomNumber);
        }
        room.setPricePerNight(newPrice);
    }

    public void removeRoom(int floorNumber, String roomNumber) {
        requireHostel();
        Floor floor = hostel.getFloor(floorNumber);
        if (floor == null) {
            throw new IllegalArgumentException("Floor " + floorNumber + " not found");
        }
        floor.removeRoom(roomNumber);
    }

    public List<Reservation> manageReservations() {
        requireHostel();
        return hostel.getReservations();
    }

    public BigDecimal managePayments() {
        requireHostel();
        return hostel.getRevenue();
    }

    public String generateReports() {
        requireHostel();
        long activeReservations = hostel.getReservations().stream()
                .filter(Reservation::isActive)
                .count();
        return "Hostel: " + hostel.getName()
                + " | Rooms: " + hostel.getAllRooms().size()
                + " | Active reservations: " + activeReservations
                + " | Revenue: " + hostel.getRevenue();
    }

    private void requireHostel() {
        if (hostel == null) {
            throw new IllegalStateException("This manager is not assigned to a hostel yet");
        }
    }
}
