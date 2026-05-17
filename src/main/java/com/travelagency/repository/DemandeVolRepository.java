package com.travelagency.repository;

import com.travelagency.entity.DemandeVol;
import com.travelagency.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DemandeVolRepository extends JpaRepository<DemandeVol, Long> {
    List<DemandeVol> findByUserOrderByCreatedAtDesc(User user);
    List<DemandeVol> findAllByOrderByCreatedAtDesc();
    Optional<DemandeVol> findByNumeroDemande(String numeroDemande);
}
