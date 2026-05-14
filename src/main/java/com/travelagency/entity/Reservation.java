package com.travelagency.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id", nullable = false)
    private Voyage voyage;

    @Column(nullable = false)
    private int nombrePersonnes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixTotal;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    private String notes;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Paiement> paiements;

    public enum StatutReservation {
        EN_ATTENTE, CONFIRMEE, PAYEE, ANNULEE
    }

    public BigDecimal getMontantPaye() {
        if (paiements == null) return BigDecimal.ZERO;
        return paiements.stream()
                .filter(p -> p.getStatut() == Paiement.StatutPaiement.SUCCES)
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getMontantRestant() {
        return prixTotal.subtract(getMontantPaye());
    }
}
