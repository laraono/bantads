package com.bantads.entity.event;

public class Event {
    private UUID eventId;
    private String objectId;
    private EventType eventType;
    private long version;
    private Map payload ;
    private LocalDateTime createdAt;

}