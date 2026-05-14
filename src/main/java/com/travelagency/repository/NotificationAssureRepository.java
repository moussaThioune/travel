package com.travelagency.repository;

import com.travelagency.entity.NotificationAssure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationAssureRepository extends JpaRepository<NotificationAssure, Long> {
    List<NotificationAssure> findByAssureIdOrderByEnvoyeAtDesc(Long assureId);
    long countByAssureIdAndSucces(Long assureId, boolean succes);
}
