package com.example.catalog.application.port.out;

import com.example.catalog.domain.Product;
import java.util.Optional;
import java.util.UUID;

/** Outbound port — what this context needs from the outside (an adapter implements it). */
public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(UUID id);
}
