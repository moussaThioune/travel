package com.travelagency.service;

import com.travelagency.entity.Paiement;
import com.travelagency.entity.Reservation;
import com.travelagency.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private static final String BREVO_API = "https://api.brevo.com/v3/smtp/email";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FORMAT  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Value("${app.brevo.api-key}") private String apiKey;
    @Value("${app.mail.from}") private String fromEmail;
    @Value("${app.mail.from-name}") private String fromName;

    private final RestTemplate rest = new RestTemplate();

    @Async
    public void sendEmailVerification(User user, String verificationLink) {
        send(user.getEmail(), user.getFirstName(),
            "✅ Activez votre compte Yatou Voyage",
            emailVerification(user, verificationLink));
    }

    @Async
    public void sendReservationConfirmation(Reservation r) {
        send(r.getClient().getEmail(), r.getClient().getFullName(),
            "✈️ Confirmation réservation #" + r.getNumeroReservation(),
            emailReservation(r));
    }

    @Async
    public void sendPaiementConfirmation(Paiement p) {
        send(p.getReservation().getClient().getEmail(), p.getReservation().getClient().getFullName(),
            "💳 Paiement confirmé - #" + p.getNumeroPaiement(),
            emailPaiement(p));
    }

    @Async
    public void sendMobileMoneyPending(Paiement p) {
        String provider = switch (p.getModePaiement()) {
            case ORANGE_MONEY -> "🟠 Orange Money";
            case WAVE -> "🔵 Wave";
            case FREE_MONEY -> "🟢 Free Money";
            default -> "Mobile Money";
        };
        send(p.getReservation().getClient().getEmail(), p.getReservation().getClient().getFullName(),
            "📱 Paiement " + provider + " en cours de validation",
            emailMobileMoneyPending(p, provider));
    }

    @Async
    public void sendPaiementRejete(Paiement p, String reason) {
        send(p.getReservation().getClient().getEmail(), p.getReservation().getClient().getFullName(),
            "❌ Paiement rejeté - " + p.getNumeroPaiement(),
            emailRejete(p, reason));
    }

    @Async
    public void sendReservationCancellation(Reservation r) {
        send(r.getClient().getEmail(), r.getClient().getFullName(),
            "❌ Annulation réservation #" + r.getNumeroReservation(),
            emailAnnulation(r));
    }

    private void send(String toEmail, String toName, String subject, String html) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> body = Map.of(
                "sender",  Map.of("name", fromName, "email", fromEmail),
                "to",      java.util.List.of(Map.of("email", toEmail, "name", toName)),
                "subject", subject,
                "htmlContent", html
            );

            ResponseEntity<String> response = rest.postForEntity(
                BREVO_API, new HttpEntity<>(body, headers), String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email envoyé via Brevo à {}: {}", toEmail, subject);
            } else {
                log.error("Brevo erreur {} pour {}: {}", response.getStatusCode(), toEmail, response.getBody());
            }
        } catch (Exception e) {
            log.error("Email ECHEC pour {}: {} — {}", toEmail, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ===== TEMPLATES =====
    private String wrap(String color, String title, String body) {
        return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><style>
        body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}
        .c{max-width:600px;margin:30px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)}
        .h{background:%s;padding:40px 30px;text-align:center;color:white}
        .h h1{margin:0;font-size:26px}.h p{margin:8px 0 0;opacity:.85}
        .b{padding:32px}.row{display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:14px}
        .lbl{color:#777}.val{font-weight:bold;color:#333}
        .card{background:#f8f9ff;border-left:4px solid %s;border-radius:6px;padding:20px;margin:16px 0}
        .amt{font-size:36px;font-weight:900;text-align:center;color:%s;padding:16px}
        .footer{background:#f8f8f8;padding:20px;text-align:center;color:#999;font-size:12px}
        .badge{display:inline-block;padding:8px 20px;border-radius:20px;font-weight:bold;font-size:14px}
        </style></head><body><div class="c">
        <div class="h"><h1>%s</h1></div>
        <div class="b">%s</div>
        <div class="footer">✈️ Yatou Voyage - yvasvoye@gmail.com | +221 78 143 44 44</div>
        </div></body></html>
        """.formatted(color, color, color, title, body);
    }

    private String emailVerification(User user, String link) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Merci de vous être inscrit sur <strong>Yatou Voyage</strong>. Il ne reste qu'une étape pour activer votre compte.</p>
        <div style="text-align:center;margin:32px 0">
          <a href="%s" style="background:linear-gradient(135deg,#1a6b6b,#0d4040);color:white;text-decoration:none;padding:16px 36px;border-radius:30px;font-size:16px;font-weight:700;display:inline-block;letter-spacing:0.5px">
            ✅ Activer mon compte
          </a>
        </div>
        <p style="color:#888;font-size:13px;text-align:center">Ce lien est valable 24 heures. Si vous n'êtes pas à l'origine de cette inscription, ignorez cet email.</p>
        <p style="color:#aaa;font-size:12px;text-align:center;word-break:break-all">Lien: %s</p>
        """.formatted(user.getFirstName(), link, link);
        return wrap("linear-gradient(135deg,#1a6b6b,#0d4040)", "✈️ Activez votre compte Yatou Voyage", body);
    }

    private String emailReservation(Reservation r) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Votre réservation a été enregistrée avec succès!</p>
        <div class="card">
            <div class="row"><span class="lbl">N° Réservation</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Voyage</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Destination</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Départ</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Retour</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Personnes</span><span class="val">%d</span></div>
        </div>
        <div class="amt">%s CFA</div>
        <p style="color:#888;font-size:14px;text-align:center">Vous pouvez payer par Orange Money, Wave, Free Money ou carte bancaire.</p>
        """.formatted(
            r.getClient().getFullName(), r.getNumeroReservation(),
            r.getVoyage().getTitre(), r.getVoyage().getDestination() + ", " + r.getVoyage().getPaysDestination(),
            r.getVoyage().getDateDepart().format(DATE_FORMAT),
            r.getVoyage().getDateRetour().format(DATE_FORMAT),
            r.getNombrePersonnes(), r.getPrixTotal()
        );
        return wrap("linear-gradient(135deg,#1a6b6b,#0d4040)", "✈️ Yatou Voyage — Réservation Confirmée", body);
    }

    private String emailPaiement(Paiement p) {
        Reservation r = p.getReservation();
        String modeLabel = switch (p.getModePaiement()) {
            case ORANGE_MONEY -> "🟠 Orange Money";
            case WAVE -> "🔵 Wave";
            case FREE_MONEY -> "🟢 Free Money";
            case CARTE_BANCAIRE -> "💳 Carte bancaire";
            case PAYPAL -> "🔵 PayPal";
            case VIREMENT -> "🏦 Virement";
            default -> p.getModePaiement().name();
        };
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Votre paiement a été validé et confirmé ✅</p>
        <div class="amt">%s %s</div>
        <div class="card">
            <div class="row"><span class="lbl">N° Paiement</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Réservation</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Mode</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Date</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Total payé</span><span class="val">%s CFA</span></div>
            <div class="row"><span class="lbl">Reste à payer</span><span class="val">%s CFA</span></div>
        </div>
        """.formatted(
            r.getClient().getFullName(),
            p.getMontant(), p.isMobileMoney() ? "FCFA" : "CFA",
            p.getNumeroPaiement(), r.getNumeroReservation(),
            modeLabel, p.getDatePaiement().format(DT_FORMAT),
            r.getMontantPaye(), r.getMontantRestant()
        );
        return wrap("linear-gradient(135deg,#16a34a,#14532d)", "💳 Paiement Confirmé!", body);
    }

    private String emailMobileMoneyPending(Paiement p, String provider) {
        Reservation r = p.getReservation();
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Votre paiement <strong>%s</strong> a bien été reçu et est en cours de validation.</p>
        <div class="card">
            <div class="row"><span class="lbl">N° Paiement</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Réservation</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Montant</span><span class="val">%s FCFA</span></div>
            <div class="row"><span class="lbl">Téléphone</span><span class="val">%s</span></div>
        </div>
        <p style="background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:14px;color:#856404">
        ⏳ <strong>Validation sous 2 à 24 heures.</strong> Vous recevrez un email de confirmation dès que notre équipe aura vérifié votre paiement.
        </p>
        """.formatted(
            r.getClient().getFullName(), provider,
            p.getNumeroPaiement(), r.getNumeroReservation(),
            p.getMontant(), p.getPhoneNumber() != null ? p.getPhoneNumber() : "—"
        );
        return wrap("linear-gradient(135deg,#d97706,#92400e)", "📱 Paiement en attente de validation", body);
    }

    private String emailRejete(Paiement p, String reason) {
        Reservation r = p.getReservation();
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Malheureusement, votre paiement <strong>%s</strong> n'a pas pu être validé.</p>
        <div class="card">
            <div class="row"><span class="lbl">N° Paiement</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Réservation</span><span class="val">%s</span></div>
            <div class="row"><span class="lbl">Montant</span><span class="val">%s FCFA</span></div>
            <div class="row"><span class="lbl">Raison</span><span class="val">%s</span></div>
        </div>
        <p>Veuillez retenter le paiement ou contacter notre service client pour assistance.</p>
        """.formatted(
            r.getClient().getFullName(), p.getNumeroPaiement(),
            p.getNumeroPaiement(), r.getNumeroReservation(),
            p.getMontant(), reason != null ? reason : "Paiement non reçu"
        );
        return wrap("linear-gradient(135deg,#dc2626,#7f1d1d)", "❌ Paiement Rejeté", body);
    }

    private String emailAnnulation(Reservation r) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Votre réservation <strong>#%s</strong> pour <strong>%s</strong> a été annulée.</p>
        <p style="color:#888">Si vous avez effectué des paiements, le remboursement sera traité sous 5-7 jours ouvrables.</p>
        """.formatted(r.getClient().getFullName(), r.getNumeroReservation(), r.getVoyage().getTitre());
        return wrap("linear-gradient(135deg,#dc2626,#7f1d1d)", "❌ Réservation Annulée", body);
    }
}
