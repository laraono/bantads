package com.bantads.repository;

import com.bantads.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, Long> {
    boolean existsBySagaIdAndType(UUID sagaId, String type);
}
