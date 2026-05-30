package com.example.catalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, UUID> {}
