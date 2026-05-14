package com.travelagency.service;

import com.travelagency.entity.Assure;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssureEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}") private String fromEmail;
    @Value("${app.mail.from-name}") private String fromName;
    @Value("${app.assurance.nom:Agence Assurance}") private String nomAgence;
    @Value("${app.assurance.tel:+221 78 143 44 44}") private String telAgence;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Async
    public void sendRappelEcheance(Assure a, String messagePersonnalise) {
        if (a.getEmail() == null || a.getEmail().isBlank()) return;
        try {
            long jours = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), a.getEcheance());
            String subject = jours <= 0
                ? "🔴 URGENT : Votre assurance a expiré — " + a.getImmatricule()
                : "⚠️ Rappel : Votre assurance expire dans " + jours + " jour(s) — " + a.getImmatricule();
            sendHtml(a.getEmail(), subject, buildEmailHtml(a, jours, messagePersonnalise));
            log.info("Email rappel envoyé à {}", a.getEmail());
        } catch (Exception e) {
            log.error("Erreur email rappel assure {}: {}", a.getId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void sendHtml(String to, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
        h.setFrom(fromEmail, fromName);
        h.setTo(to); h.setSubject(subject);
        h.setText(html, true);
        mailSender.send(msg);
    }

    private String buildEmailHtml(Assure a, long jours, String msgPerso) {
        String couleur = jours <= 0 ? "#dc2626" : jours <= 7 ? "#d97706" : "#1a6b6b";
        String badgeTxt = jours <= 0 ? "EXPIRÉ" : jours <= 7 ? "URGENT" : "RAPPEL";
        String msgPersoBlock = msgPerso != null && !msgPerso.isBlank()
            ? "<p style='background:#f0f9ff;border-left:4px solid #0ea5e9;padding:14px;border-radius:6px;margin:16px 0'>" + msgPerso + "</p>"
            : "";

        return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><style>
        body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}
        .c{max-width:600px;margin:30px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)}
        .h{background:%s;padding:36px 30px;text-align:center;color:white}
        .h h1{margin:0;font-size:24px;font-weight:800}.badge{display:inline-block;background:rgba(255,255,255,.2);padding:4px 14px;border-radius:20px;font-size:13px;font-weight:700;margin-bottom:10px}
        .b{padding:32px}.row{display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:14px}
        .lbl{color:#777}.val{font-weight:700;color:#333}
        .card{background:#f8f9ff;border-left:4px solid %s;border-radius:6px;padding:18px;margin:16px 0}
        .jours{font-size:42px;font-weight:900;color:%s;text-align:center;padding:16px 0}
        .footer{background:#f8f8f8;padding:20px;text-align:center;color:#999;font-size:12px}
        .cta{display:inline-block;background:%s;color:white;padding:12px 28px;border-radius:25px;text-decoration:none;font-weight:700;margin:16px 0}
        </style></head><body><div class="c">
        <div class="h">
          <div class="badge">%s</div>
          <h1>🚗 Rappel Assurance Véhicule</h1>
          <p style="margin:8px 0 0;opacity:.9">%s</p>
        </div>
        <div class="b">
          <p>Bonjour <strong>%s</strong>,</p>
          <p>Nous vous informons que votre assurance véhicule arrive à échéance.</p>
          %s
          <div class="jours">%s</div>
          <div class="card">
            <div class="row"><span class="lbl">Véhicule</span><span class="val">%s %s</span></div>
            <div class="row"><span class="lbl">Immatricule</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">N° Police</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Date d'échéance</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Puissance</span><span class="val">%s</span></div>
          </div>
          <p style="text-align:center"><strong>Contactez-nous pour renouveler votre contrat :</strong><br>
          📞 %s</p>
          <p style="color:#888;font-size:13px;margin-top:24px">Ne laissez pas votre véhicule sans couverture. Un renouvellement rapide vous protège contre tout imprévu.</p>
        </div>
        <div class="footer"><p>%s — Ce message est automatique, merci de ne pas y répondre.</p></div>
        </div></body></html>
        """.formatted(
            couleur, couleur, couleur, couleur,
            badgeTxt,
            nomAgence,
            a.getNomComplet(),
            msgPersoBlock,
            jours <= 0 ? "EXPIRÉ" : ("J-" + jours),
            a.getMarque() != null ? a.getMarque() : "",
            "",
            a.getImmatricule() != null ? a.getImmatricule() : "—",
            a.getNumeroPolicce() != null ? a.getNumeroPolicce() : "—",
            a.getEcheance() != null ? a.getEcheance().format(DF) : "—",
            a.getPuissanceFiscale() != null ? a.getPuissanceFiscale() : "—",
            telAgence,
            nomAgence
        );
    }
}
