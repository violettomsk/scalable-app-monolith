package com.example.catalog.adapter.in.web;

import com.example.catalog.application.port.in.CreateProductUseCase;
import com.example.catalog.domain.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products") // API versioned from day one
public class ProductController {

    private final CreateProductUseCase createProduct;

    public ProductController(CreateProductUseCase createProduct) {
        this.createProduct = createProduct;
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody CreateProductRequest req) {
        Product p = createProduct.create(req.name(), req.priceCents());
        return new ProductResponse(p.id().toString(), p.name(), p.priceCents());
    }

    // DTOs — never serialize the domain entity directly.
    public record CreateProductRequest(@NotBlank String name, @PositiveOrZero long priceCents) {}
    public record ProductResponse(String id, String name, long priceCents) {}
}
