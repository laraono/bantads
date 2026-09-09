package com.bantads.entity.event;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "event",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"object_id", "version"})}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id")
    private UUID eventId;

    @Column(nullable = false, name = "object_id", length = 4)
    private String objectId;

    @Column(nullable = false, name = "event_type")
    private EventType eventType;

    @Column(nullable = false)
    private long version;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private  Map<String, Object> payload ;

    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

}