package com.bantads.entity.read;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "request", schema = "request_db",
    indexes = {
        @Index(name = "idx_event_id", columnList = "eventId", unique = true),
        @Index(name = "idx_version", columnList = "version", unique = true)
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
    private Long request;

    @Column(nullable = false, name = "event_id")
    private UUID eventId;

    @Column(nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
