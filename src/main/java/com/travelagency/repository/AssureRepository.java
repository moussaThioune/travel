package com.travelagency.repository;

import com.travelagency.entity.Assure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AssureRepository extends JpaRepository<Assure, Long> {

    List<Assure> findByStatut(Assure.StatutAssure statut);

    List<Assure> findAllByOrderByCreatedAtDesc();

    // Assurés dont l'échéance arrive dans les N prochains jours
    @Query("SELECT a FROM Assure a WHERE a.echeance BETWEEN :now AND :limit AND a.statut = 'ACTIF' ORDER BY a.echeance ASC")
    List<Assure> findEcheancesProches(@Param("now") LocalDate now, @Param("limit") LocalDate limit);

    // Assurés expirés (échéance passée)
    @Query("SELECT a FROM Assure a WHERE a.echeance < :now AND a.statut = 'ACTIF'")
    List<Assure> findExpires(@Param("now") LocalDate now);

    // Dont le rappel est aujourd'hui ou dans les jours passés non encore notifiés
    @Query("SELECT a FROM Assure a WHERE a.dateRappel <= :today AND a.echeance >= :today AND a.statut = 'ACTIF'")
    List<Assure> findARappeler(@Param("today") LocalDate today);

    // Recherche full-text
    @Query("SELECT a FROM Assure a WHERE LOWER(a.nom) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(a.prenom) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(a.immatricule) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(a.telephone) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(a.marque) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Assure> search(@Param("q") String query);

    // Stats par mois d'échéance
    @Query("SELECT MONTH(a.echeance), COUNT(a) FROM Assure a WHERE YEAR(a.echeance) = :year GROUP BY MONTH(a.echeance)")
    List<Object[]> countByMoisEcheance(@Param("year") int year);

    long countByStatut(Assure.StatutAssure statut);
}
