package com.bantads.repository.event;

import com.bantads.entity.event.Event;
import com.bantads.entity.event.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Event findFirstByObjectIdOrderByCreatedAtDesc(String objectId);

    List<Event> findAllByObjectIdOrderByVersionAsc(String objectId);

    boolean existsByObjectIdAndEventType(String objectId, EventType eventType);
}