package com.hotel.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class User {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public User(String firstName, String lastName, String email, String phone) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}