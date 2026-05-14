package com.travelagency.repository;

import com.travelagency.entity.Voyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VoyageRepository extends JpaRepository<Voyage, Long> {

    List<Voyage> findByStatut(Voyage.StatutVoyage statut);

    @Query("SELECT v FROM Voyage v WHERE v.statut = 'ACTIF' " +
           "AND (:destination IS NULL OR LOWER(v.destination) LIKE LOWER(CONCAT('%', :destination, '%'))) " +
           "AND (:dateDepart IS NULL OR v.dateDepart >= :dateDepart) " +
           "AND (:prixMax IS NULL OR v.prixParPersonne <= :prixMax) " +
           "AND (:places IS NULL OR v.nombrePlacesDisponibles >= :places)")
    List<Voyage> searchVoyages(
            @Param("destination") String destination,
            @Param("dateDepart") LocalDate dateDepart,
            @Param("prixMax") BigDecimal prixMax,
            @Param("places") Integer places
    );

    @Query("SELECT v FROM Voyage v WHERE v.nombrePlacesDisponibles > 0 AND v.statut = 'ACTIF' ORDER BY v.dateDepart")
    List<Voyage> findAvailableVoyages();
}
