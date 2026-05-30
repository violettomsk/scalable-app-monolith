package com.example.catalog.application;

import com.example.catalog.application.port.in.CreateProductUseCase;
import com.example.catalog.application.port.out.ProductRepositoryPort;
import com.example.catalog.domain.Product;
import org.springframework.stereotype.Service;
import java.util.UUID;

/** Use-case implementation. Depends only on the domain and outbound ports. */
@Service
public class CreateProductService implements CreateProductUseCase {

    private final ProductRepositoryPort repository;

    public CreateProductService(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Product create(String name, long priceCents) {
        return repository.save(new Product(UUID.randomUUID(), name, priceCents));
    }
}
