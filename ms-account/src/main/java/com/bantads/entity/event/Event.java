package com.bantads.entity.event;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class Event {
    private UUID eventId;
    private String objectId;
    private EventType eventType;
    private long version;
    private Map payload ;
    private Date createdAt;

}