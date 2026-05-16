package com.travelagency.repository;

import com.travelagency.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {

    List<Avis> findAllByOrderByDateDesc();

    @Query("SELECT COALESCE(AVG(CAST(a.note AS double)), 0) FROM Avis a")
    Double averageNote();

    long count();
}
