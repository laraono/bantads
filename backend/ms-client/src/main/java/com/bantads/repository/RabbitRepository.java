package com.bantads.repository;

import com.bantads.entity.Rabbit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RabbitRepository extends JpaRepository<Rabbit, Long> {
    boolean existsBySagaIdAndType(UUID sagaId, String type);

}
