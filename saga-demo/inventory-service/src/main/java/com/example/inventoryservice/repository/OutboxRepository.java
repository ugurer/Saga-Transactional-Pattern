package com.example.inventoryservice.repository;

import com.example.inventoryservice.model.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    
    List<Outbox> findByCreatedAtBefore(LocalDateTime timestamp);
} 