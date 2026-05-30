package com.example.catalog.adapter.out.persistence;

import com.example.catalog.application.port.out.ProductRepositoryPort;
import com.example.catalog.domain.Product;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

/** Adapter: maps domain <-> JPA. Implements the outbound port. */
@Component
class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository jpa;

    ProductPersistenceAdapter(SpringDataProductRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product save(Product p) {
        jpa.save(new ProductJpaEntity(p.id(), p.name(), p.priceCents()));
        return p;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpa.findById(id).map(e -> new Product(e.id, e.name, e.priceCents));
    }
}
