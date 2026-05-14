package com.travelagency.repository;

import com.travelagency.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByReservationId(Long reservationId);
    List<Paiement> findByStatut(Paiement.StatutPaiement statut);
    List<Paiement> findByModePaiement(Paiement.ModePaiement modePaiement);

    @Query("SELECT p FROM Paiement p WHERE p.modePaiement IN ('ORANGE_MONEY','WAVE','FREE_MONEY') AND p.statut = 'EN_ATTENTE' ORDER BY p.datePaiement DESC")
    List<Paiement> findMobileMoneyEnAttente();

    @Query("SELECT p FROM Paiement p WHERE p.modePaiement IN ('ORANGE_MONEY','WAVE','FREE_MONEY') ORDER BY p.datePaiement DESC")
    List<Paiement> findAllMobileMoney();

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.statut = 'SUCCES' AND p.modePaiement = :mode")
    BigDecimal sumByModeAndSucces(@Param("mode") Paiement.ModePaiement mode);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.statut = 'SUCCES'")
    BigDecimal sumAllSucces();

    @Query("SELECT COUNT(p) FROM Paiement p WHERE p.statut = 'EN_ATTENTE' AND p.modePaiement IN ('ORANGE_MONEY','WAVE','FREE_MONEY')")
    long countMobileMoneyEnAttente();
}
