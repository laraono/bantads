package com.bantads.entity;

import jakarta.persistence.*;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "request", schema = "client_db")
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false)
    @Builder.Default
    private String status = RequestStatus.PENDING.getLabel();

    @Column(nullable = true, length = 255, name = "rejection_reason")
    private String rejectionReason;
    
    @Column(nullable = true, name = "approved_at")
    private Date approvedAt;
}