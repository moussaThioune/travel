package com.travelagency.service;

import com.travelagency.entity.Paiement;
import com.travelagency.entity.Reservation;
import com.travelagency.entity.User;
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
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}") private String fromEmail;
    @Value("${app.mail.from-name}") private String fromName;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FORMAT  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Async
    public void sendEmailVerification(User user, String verificationLink) {
        try {
            sendHtml(user.getEmail(),
                "✅ Activez votre compte Yatou Voyage",
                emailVerification(user, verificationLink));
        } catch (Exception e) {
            log.error("Email vérification ECHEC pour {}: {} — {}", user.getEmail(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Async
    public void sendReservationConfirmation(Reservation r) {
        try {
            sendHtml(r.getClient().getEmail(),
                "✈️ Confirmation réservation #" + r.getNumeroReservation(),
                emailReservation(r));
        } catch (Exception e) { log.error("Email confirmation: {}", e.getMessage()); }
    }

    @Async
    public void sendPaiementConfirmation(Paiement p) {
        try {
            sendHtml(p.getReservation().getClient().getEmail(),
                "💳 Paiement confirmé - #" + p.getNumeroPaiement(),
                emailPaiement(p));
        } catch (Exception e) { log.error("Email paiement: {}", e.getMessage()); }
    }

    @Async
    public void sendMobileMoneyPending(Paiement p) {
        try {
            String provider = switch (p.getModePaiement()) {
                case ORANGE_MONEY -> "🟠 Orange Money";
                case WAVE -> "🔵 Wave";
                case FREE_MONEY -> "🟢 Free Money";
                default -> "Mobile Money";
            };
            sendHtml(p.getReservation().getClient().getEmail(),
                "📱 Paiement " + provider + " en cours de validation",
                emailMobileMoneyPending(p, provider));
        } catch (Exception e) { log.error("Email mobile money pending: {}", e.getMessage()); }
    }

    @Async
    public void sendPaiementRejete(Paiement p, String reason) {
        try {
            sendHtml(p.getReservation().getClient().getEmail(),
                "❌ Paiement rejeté - " + p.getNumeroPaiement(),
                emailRejete(p, reason));
        } catch (Exception e) { log.error("Email rejet: {}", e.getMessage()); }
    }

    @Async
    public void sendReservationCancellation(Reservation r) {
        try {
            sendHtml(r.getClient().getEmail(),
                "❌ Annulation réservation #" + r.getNumeroReservation(),
                emailAnnulation(r));
        } catch (Exception e) { log.error("Email annulation: {}", e.getMessage()); }
    }

    private void sendHtml(String to, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
        h.setFrom(fromEmail, fromName);
        h.setTo(to); h.setSubject(subject);
        h.setText(html, true);
        mailSender.send(msg);
        log.info("Email envoyé à {}: {}", to, subject);
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
        <div class="footer">✈️ Yatou Voyage - contact@voyageur.fr | +221 33 00 00 00</div>
        </div></body></html>
        """.formatted(color, color, color, title, body);
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
        <div class="amt">%s €</div>
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
            <div class="row"><span class="lbl">Total payé</span><span class="val">%s €</span></div>
            <div class="row"><span class="lbl">Reste à payer</span><span class="val">%s €</span></div>
        </div>
        """.formatted(
            r.getClient().getFullName(),
            p.getMontant(), p.isMobileMoney() ? "FCFA" : "€",
            p.getNumeroPaiement(), r.getNumeroReservation(),
            modeLabel,
            p.getDatePaiement().format(DT_FORMAT),
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

    private String emailAnnulation(Reservation r) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Votre réservation <strong>#%s</strong> pour <strong>%s</strong> a été annulée.</p>
        <p style="color:#888">Si vous avez effectué des paiements, le remboursement sera traité sous 5-7 jours ouvrables.</p>
        """.formatted(r.getClient().getFullName(), r.getNumeroReservation(), r.getVoyage().getTitre());
        return wrap("linear-gradient(135deg,#dc2626,#7f1d1d)", "❌ Réservation Annulée", body);
    }
}
