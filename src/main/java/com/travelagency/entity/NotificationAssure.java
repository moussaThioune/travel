package com.travelagency.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications_assures")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationAssure {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assure_id", nullable = false)
    private Assure assure;

    @Enumerated(EnumType.STRING)
    private TypeNotif type;  // EMAIL, SMS

    private boolean succes;
    private String message;  // Corps du message

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime envoyeAt = LocalDateTime.now();

    private String erreur;   // En cas d'échec

    public enum TypeNotif { EMAIL, SMS }
}
