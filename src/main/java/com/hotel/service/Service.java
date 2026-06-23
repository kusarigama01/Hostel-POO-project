package com.hotel.service;

import java.math.BigDecimal;
import java.util.UUID;

public class Service {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;

    public Service(String name, String description, BigDecimal price) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = true;
    }

    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
    public void activate() { this.active = true; }
    public void cancel() { this.active = false; }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
}