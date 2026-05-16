package com.travelagency.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "avis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 150)
    private String voyage;

    @Column(nullable = false)
    private Integer note;

    @Column(nullable = false, length = 500)
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime date;

    @PrePersist
    void prePersist() {
        if (date == null) date = LocalDateTime.now();
    }
}
