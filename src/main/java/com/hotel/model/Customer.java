package com.hotel.model;

import java.time.LocalDate;

public class Customer extends User {
    private LocalDate dateOfBirth;
    private Address address;

    public Customer(String firstName, String lastName, String email, String phone, LocalDate dateOfBirth, Address address) {
        super(firstName, lastName, email, phone);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Address getAddress() { return address; }
}