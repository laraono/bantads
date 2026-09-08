package com.bantads.entity.event;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EventTypeConverter implements AttributeConverter<EventType, String> {

    @Override
    public String convertToDatabaseColumn(EventType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLabel(); 
    }

    @Override
    public EventType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (EventType type : EventType.values()) {
            if (type.getLabel().equalsIgnoreCase(dbData)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown database value: " + dbData);
    }
}