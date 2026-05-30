package com.example.catalog.application.port.in;

import com.example.catalog.domain.Product;

/** Inbound port — what the world can ask this context to do. */
public interface CreateProductUseCase {
    Product create(String name, long priceCents);
}
