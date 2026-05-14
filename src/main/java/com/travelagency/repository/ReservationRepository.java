package com.travelagency.repository;

import com.travelagency.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClientId(Long clientId);
    List<Reservation> findByVoyageId(Long voyageId);
    Optional<Reservation> findByNumeroReservation(String numeroReservation);
    List<Reservation> findByStatut(Reservation.StatutReservation statut);
    long countByVoyageId(Long voyageId);
}
