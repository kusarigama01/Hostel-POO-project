package com.hotel.model;

public class Manager extends User {
    private String employeeId;

    public Manager(String firstName, String lastName, String email, String phone, String employeeId) {
        super(firstName, lastName, email, phone);
        this.employeeId = employeeId;
    }

    public String getEmployeeId() { return employeeId; }
}