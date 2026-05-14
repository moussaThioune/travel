package com.travelagency.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssureSchedulerJob {

    private final AssureService assureService;

    /**
     * Tourne le 25 de chaque mois à 08h00
     * cron = "seconde minute heure jour mois jour-semaine"
     */
    @Scheduled(cron = "0 0 8 25 * *")
    public void rappelsMensuelsDu25() {
        log.info("🗓️ [JOB] Rappels mensuel du 25 — démarrage à {}", LocalDateTime.now());
        assureService.envoyerRappelsMensuelsTous();
        log.info("🗓️ [JOB] Rappels mensuel du 25 — terminé");
    }

    /**
     * Chaque jour à 07h00 : notifier les assurés dont le rappel est AUJOURD'HUI
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void rappelsQuotidiens() {
        log.info("🔔 [JOB] Rappels quotidiens — démarrage à {}", LocalDateTime.now());
        assureService.envoyerRappelsDuJour();
        log.info("🔔 [JOB] Rappels quotidiens — terminé");
    }
}
