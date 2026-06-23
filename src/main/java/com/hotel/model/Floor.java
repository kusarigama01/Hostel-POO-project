package com.hotel.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Floor {
    private final UUID id;
    private final int number;
    private final List<Room> rooms;

    public Floor(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("Floor number must be positive");
        }
        this.id = UUID.randomUUID();
        this.number = number;
        this.rooms = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public void addRoom(Room room) {
        Objects.requireNonNull(room, "room must not be null");
        if (getRoomByNumber(room.getNumber()) != null) {
            throw new IllegalArgumentException("Room number already exists on this floor");
        }
        rooms.add(room);
    }

    public Room getRoomByNumber(String roomNumber) {
        for (Room room : rooms) {
            if (room.getNumber().equals(roomNumber)) {
                return room;
            }
        }
        return null;
    }

    public Room removeRoom(String roomNumber) {
        Room room = getRoomByNumber(roomNumber);
        if (room != null) {
            rooms.remove(room);
        }
        return room;
    }
}
