package com.bantads.repository.read;

import com.bantads.entity.read.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, Long> {
    boolean existsByVersionAndEventId(long version, UUID eventId);
}
