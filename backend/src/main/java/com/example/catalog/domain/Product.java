package com.example.catalog.domain;

import java.util.UUID;

/** Pure domain entity. No Spring, JPA, or serialization imports — ever. */
public final class Product {
    private final UUID id;
    private final String name;
    private final long priceCents;

    public Product(UUID id, String name, long priceCents) {
        if (id == null) throw new IllegalArgumentException("id required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (priceCents < 0) throw new IllegalArgumentException("price must be >= 0");
        this.id = id;
        this.name = name;
        this.priceCents = priceCents;
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public long priceCents() { return priceCents; }
}
