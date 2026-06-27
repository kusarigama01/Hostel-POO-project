package com.hotel.service;

import com.hotel.model.*;

import java.util.List;
import java.util.Objects;

public class ServiceRoomManagement {
    public void markMaintenance(Room room) {
        Objects.requireNonNull(room, "room must not be null");
        room.setStatus(RoomStatus.MAINTENANCE);
    }

    public void markAvailable(Room room) {
        Objects.requireNonNull(room, "room must not be null");
        room.setStatus(RoomStatus.AVAILABLE);
    }

    public void markOutOfOrder(Room room) {
        Objects.requireNonNull(room, "room must not be null");
        room.setStatus(RoomStatus.OUT_OF_ORDER);
    }

    public List<Room> listAvailableRooms(Hostel hostel, RoomType roomType, DateRange dateRange) {
        Objects.requireNonNull(hostel, "hostel must not be null");
        return hostel.getAvailableRooms(roomType, dateRange);
    }
}
