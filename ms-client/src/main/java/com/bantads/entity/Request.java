package com.bantads.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long requestId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDENTE;

    @Column(nullable = true, length = 255, name = "rejection_reason")
    private String rejectionReason;
    
    @Column(nullable = true, name = "approved_at")
    private Date approvedAt;
}