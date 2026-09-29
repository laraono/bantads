package com.bantads.entity.read;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "request",
    indexes = {
        @Index(name = "idx_event_id", columnList = "event_id"),
        @Index(name = "idx_version", columnList = "version")
    },
    uniqueConstraints = {
            @UniqueConstraint(columnNames = {"event_id", "version"}),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "request_id")
    private Long requestId;

    @Column(nullable = true, name = "event_id")
    private UUID eventId;

    @Column(nullable = true)
    private long version;

    @Column(nullable = true)
    private String type;

    @Column(nullable = true, name = "saga_id")
    private UUID sagaId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
