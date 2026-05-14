package com.travelagency.service;

import com.travelagency.entity.Assure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class AssureSmsService {

    @Value("${app.sms.enabled:false}") private boolean smsEnabled;
    @Value("${app.assurance.nom:Agence Assurance}") private String nomAgence;
    @Value("${app.assurance.tel:+221 78 143 44 44}") private String telAgence;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Async
    public void sendRappel(Assure a, String messagePerso) {
        String tel = normaliserTel(a.getTelephone());
        if (tel == null) {
            log.warn("SMS: numéro invalide pour assuré {}", a.getId());
            return;
        }
        long jours = a.getEcheance() != null
            ? java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), a.getEcheance()) : 0;

        String msg = messagePerso != null && !messagePerso.isBlank()
            ? messagePerso
            : buildSmsText(a, jours);

        if (smsEnabled) {
            // === Twilio (décommenter et configurer) ===
            // com.twilio.Twilio.init(accountSid, authToken);
            // com.twilio.rest.api.v2010.account.Message.creator(
            //     new com.twilio.type.PhoneNumber(tel),
            //     new com.twilio.type.PhoneNumber(fromNumber),
            //     msg
            // ).create();
            log.info("SMS Twilio → {} : {}", tel, msg);
        } else {
            // Mode simulation : log uniquement
            log.info("📱 [SMS SIMULATION] → {} :\n{}", tel, msg);
        }
    }

    private String buildSmsText(Assure a, long jours) {
        String echeanceStr = a.getEcheance() != null ? a.getEcheance().format(DF) : "?";
        if (jours <= 0) {
            return String.format(
                "URGENT - %s, votre assurance %s (%s) a EXPIRE le %s. " +
                "Renouvelez maintenant au %s. - %s",
                a.getNomComplet(),
                a.getMarque() != null ? a.getMarque() : "véhicule",
                a.getImmatricule() != null ? a.getImmatricule() : "",
                echeanceStr, telAgence, nomAgence
            );
        }
        return String.format(
            "Rappel %s: Votre assurance %s (%s) expire le %s (J-%d). " +
            "Contactez-nous: %s - %s",
            a.getNomComplet(),
            a.getMarque() != null ? a.getMarque() : "véhicule",
            a.getImmatricule() != null ? a.getImmatricule() : "",
            echeanceStr, jours, telAgence, nomAgence
        );
    }

    private String normaliserTel(String tel) {
        if (tel == null || tel.isBlank()) return null;
        String clean = tel.replaceAll("[\\s\\-\\.]", "");
        if (clean.startsWith("0")) clean = "+221" + clean.substring(1);
        else if (clean.startsWith("7") || clean.startsWith("3")) clean = "+221" + clean;
        else if (!clean.startsWith("+")) clean = "+" + clean;
        return clean.length() >= 10 ? clean : null;
    }
}
