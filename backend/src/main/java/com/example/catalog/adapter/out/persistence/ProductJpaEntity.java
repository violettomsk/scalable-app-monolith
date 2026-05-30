package com.example.catalog.adapter.out.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "products")
class ProductJpaEntity {
    @Id UUID id;
    String name;
    long priceCents;

    protected ProductJpaEntity() {}
    ProductJpaEntity(UUID id, String name, long priceCents) {
        this.id = id; this.name = name; this.priceCents = priceCents;
    }
}
