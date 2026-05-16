package com.travelagency.service;

import com.travelagency.entity.Avis;
import com.travelagency.repository.AvisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvisService {

    private static final long BASE_TRAVELERS = 12_000L;
    private static final double BASE_RATING_SUM = 9_800 * 4.9;
    private static final long BASE_RATING_COUNT = 9_800L;

    private final AvisRepository avisRepository;

    public List<Avis> findAll() {
        return avisRepository.findAllByOrderByDateDesc();
    }

    @Transactional
    public Avis create(Avis avis) {
        if (avis.getNote() < 0 || avis.getNote() > 10) {
            throw new IllegalArgumentException("La note doit être entre 0 et 10");
        }
        return avisRepository.save(avis);
    }

    public Map<String, Object> getStats() {
        long count = avisRepository.count();
        Double avg = avisRepository.averageNote();

        double totalSum = BASE_RATING_SUM + (avg != null ? avg * count : 0);
        long totalCount = BASE_RATING_COUNT + count;
        double moyennefinale = totalCount > 0
                ? Math.round((totalSum / totalCount) * 10.0) / 10.0
                : 4.9;

        return Map.of(
                "totalTravelers", BASE_TRAVELERS + count,
                "averageRating", moyennefinale,
                "totalAvis", count
        );
    }
}
