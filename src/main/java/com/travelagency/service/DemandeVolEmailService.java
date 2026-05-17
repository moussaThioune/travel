package com.travelagency.service;

import com.travelagency.entity.DemandeVol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DemandeVolEmailService {

    private static final String BREVO_API = "https://api.brevo.com/v3/smtp/email";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${app.brevo.api-key:}") private String apiKey;
    @Value("${app.mail.from}") private String fromEmail;
    @Value("${app.mail.from-name}") private String fromName;
    @Value("${app.frontend.url:http://localhost:4200}") private String frontendUrl;

    private final RestTemplate rest = new RestTemplate();

    // 1. Client submits → confirmation to client
    @Async
    public void sendConfirmationClient(DemandeVol d) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Nous avons bien reçu votre demande de vol. Notre équipe va traiter votre demande et vous enverra une réponse avec les disponibilités et tarifs sous <strong>24 heures</strong>.</p>
        %s
        <p style="background:#e8f5e9;border-left:4px solid #1a6b6b;border-radius:6px;padding:16px;color:#1a6b6b;margin:20px 0">
        ⏳ <strong>Traitement en cours.</strong> Vous recevrez un email dès qu'une offre est disponible pour votre itinéraire.
        </p>
        <p>Vous pouvez suivre l'état de votre demande dans <a href="%s/mes-demandes-vols" style="color:#1a6b6b;font-weight:700">Mon espace client</a>.</p>
        """.formatted(
            d.getUser().getFirstName(),
            detailsCard(d),
            frontendUrl
        );
        send(d.getUser().getEmail(), d.getUser().getFirstName() + " " + d.getUser().getLastName(),
            "✈️ Demande de vol #" + d.getNumeroDemande() + " reçue", body);
    }

    // 2. Notify admin of new request
    @Async
    public void sendNotificationAdmin(DemandeVol d) {
        String body = """
        <p>Une nouvelle demande de vol vient d'être soumise.</p>
        <div style="background:#fff3cd;border-left:4px solid #f59e0b;border-radius:6px;padding:16px;margin:16px 0">
        <strong>⚡ Action requise</strong> — Répondre avec les disponibilités et tarifs.
        </div>
        %s
        <p style="margin-top:20px">
          <a href="%s/admin?tab=vols" style="background:#1a6b6b;color:white;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:700;display:inline-block">
            Gérer cette demande →
          </a>
        </p>
        """.formatted(detailsCard(d), frontendUrl);
        send(fromEmail, fromName,
            "🔔 Nouvelle demande vol #" + d.getNumeroDemande() + " — " + d.getOrigine() + " → " + d.getDestination(), body);
    }

    // 3. Admin sends pricing → email to client
    @Async
    public void sendReponseClient(DemandeVol d) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Bonne nouvelle ! Nous avons trouvé un vol correspondant à votre demande <strong>#%s</strong>.</p>
        <div style="background:#f0f9f0;border:2px solid #1a6b6b;border-radius:12px;padding:24px;margin:20px 0;text-align:center">
          <div style="font-size:32px;font-weight:900;color:#1a6b6b;margin-bottom:8px">%s FCFA</div>
          <div style="color:#666;font-size:14px">prix total pour %d passager(s)</div>
          <div style="font-size:18px;font-weight:700;color:#333;margin-top:12px">%s</div>
          <div style="color:#666;font-size:13px;margin-top:4px">%s%s</div>
          %s
        </div>
        %s
        <p>Pour confirmer votre réservation, cliquez sur <strong>Accepter les tarifs</strong> dans votre espace client :</p>
        <div style="text-align:center;margin:28px 0;display:flex;gap:16px;justify-content:center">
          <a href="%s/mes-demandes-vols" style="background:#1a6b6b;color:white;padding:14px 32px;border-radius:30px;text-decoration:none;font-weight:700;font-size:16px;display:inline-block">
            ✅ Voir ma demande
          </a>
        </div>
        <p style="color:#888;font-size:13px;text-align:center">Cette offre est valable 48 heures. Passé ce délai, les tarifs peuvent changer.</p>
        """.formatted(
            d.getUser().getFirstName(),
            d.getNumeroDemande(),
            formatPrice(d.getPrixTotal()),
            d.totalPassagers(),
            d.getCompagnieAerienne(),
            d.getOrigine() + " → " + d.getDestination(),
            d.getDureeVol() != null ? " · " + d.getDureeVol() : "",
            d.getEscales() != null ? "<div style='color:#e67e22;font-size:13px;margin-top:6px'>Via " + d.getEscales() + "</div>" : "",
            prixCard(d),
            frontendUrl
        );
        send(d.getUser().getEmail(), d.getUser().getFirstName() + " " + d.getUser().getLastName(),
            "✈️ Réponse à votre demande #" + d.getNumeroDemande() + " — " + d.getOrigine() + " → " + d.getDestination(), body);
    }

    // 4a. Client accepts → email to client
    @Async
    public void sendClientAccepte(DemandeVol d) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Vous avez accepté les tarifs pour votre demande de vol <strong>#%s</strong>. Notre équipe va maintenant valider votre réservation.</p>
        <div style="background:#e8f5e9;border-left:4px solid #1a6b6b;border-radius:6px;padding:16px;margin:20px 0">
        ✅ <strong>Tarifs acceptés.</strong> Validation en cours — vous serez notifié sous 2 heures.
        </div>
        %s
        """.formatted(d.getUser().getFirstName(), d.getNumeroDemande(), detailsCard(d));
        send(d.getUser().getEmail(), d.getUser().getFirstName() + " " + d.getUser().getLastName(),
            "✅ Tarifs acceptés — Demande #" + d.getNumeroDemande(), body);
    }

    // 4b. Client accepts → notify admin
    @Async
    public void sendAdminClientAccepte(DemandeVol d) {
        String body = """
        <p>Le client <strong>%s</strong> a accepté les tarifs pour la demande <strong>#%s</strong>.</p>
        <div style="background:#e8f5e9;border-left:4px solid #16a34a;border-radius:6px;padding:16px;margin:16px 0">
        ✅ <strong>Action requise</strong> — Valider la demande pour que le client puisse passer au paiement.
        </div>
        %s
        <a href="%s/admin?tab=vols" style="background:#1a6b6b;color:white;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:700;display:inline-block;margin-top:16px">
          Valider la demande →
        </a>
        """.formatted(d.getUser().getFirstName() + " " + d.getUser().getLastName(), d.getNumeroDemande(), detailsCard(d), frontendUrl);
        send(fromEmail, fromName,
            "✅ Client accepte les tarifs — #" + d.getNumeroDemande(), body);
    }

    // 5. Admin validates → email to client
    @Async
    public void sendValidationClient(DemandeVol d) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Votre demande de vol <strong>#%s</strong> est <strong>confirmée</strong> ! Vous pouvez maintenant procéder au paiement.</p>
        <div style="text-align:center;background:#f0f9f0;border-radius:12px;padding:24px;margin:20px 0">
          <div style="font-size:36px;font-weight:900;color:#1a6b6b">%s FCFA</div>
          <div style="color:#666;margin-top:4px">%s → %s</div>
          <div style="font-size:15px;font-weight:600;color:#333;margin-top:8px">%s</div>
        </div>
        <div style="text-align:center;margin:28px 0">
          <a href="%s/mes-demandes-vols" style="background:linear-gradient(135deg,#1a6b6b,#0d4040);color:white;padding:16px 40px;border-radius:30px;text-decoration:none;font-weight:700;font-size:16px;display:inline-block">
            💳 Procéder au paiement
          </a>
        </div>
        <p style="color:#888;font-size:13px;text-align:center">Contactez-nous si vous avez des questions : +221 78 143 44 44</p>
        """.formatted(
            d.getUser().getFirstName(), d.getNumeroDemande(),
            formatPrice(d.getPrixTotal()),
            d.getOrigine(), d.getDestination(),
            d.getCompagnieAerienne(),
            frontendUrl
        );
        send(d.getUser().getEmail(), d.getUser().getFirstName() + " " + d.getUser().getLastName(),
            "🎉 Vol confirmé ! Procédez au paiement — #" + d.getNumeroDemande(), body);
    }

    // 6. Client pays → confirmation email to client
    @Async
    public void sendPaiementVolClient(DemandeVol d, String mode, String phone) {
        String phoneInfo = (phone != null && !phone.isBlank())
            ? "<div style='padding:4px 0'><span style='color:#777'>Numéro : </span><strong>" + phone + "</strong></div>"
            : "";
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Nous avons bien reçu votre paiement pour la demande de vol <strong>#%s</strong>. Notre équipe va maintenant procéder à l'émission de votre billet d'avion.</p>
        <div style="background:#f0f9f0;border:2px solid #1a6b6b;border-radius:12px;padding:24px;margin:20px 0;text-align:center">
          <div style="font-size:32px;font-weight:900;color:#1a6b6b;margin-bottom:8px">%s FCFA</div>
          <div style="color:#666;font-size:14px">%s → %s</div>
          <div style="font-size:16px;font-weight:700;color:#333;margin-top:10px">%s</div>
        </div>
        <div style="background:#f8f9fa;border-radius:10px;padding:16px;margin:16px 0;font-size:14px">
          <div style="padding:4px 0"><span style="color:#777">Mode de paiement : </span><strong>%s</strong></div>
          %s
        </div>
        <div style="background:#e8f5e9;border-left:4px solid #1a6b6b;border-radius:6px;padding:16px;margin:20px 0">
        ⏳ <strong>Billet en cours d'émission.</strong> Vous recevrez votre numéro de billet par email dès qu'il sera disponible.
        </div>
        <p>Vous pouvez suivre l'état de votre réservation dans <a href="%s/mes-demandes-vols" style="color:#1a6b6b;font-weight:700">Mon espace client</a>.</p>
        <p style="color:#888;font-size:13px">Merci de votre confiance. 🙏<br><strong>L'équipe YVAS</strong></p>
        """.formatted(
            d.getUser().getFirstName(),
            d.getNumeroDemande(),
            formatPrice(d.getPrixTotal()),
            d.getOrigine(), d.getDestination(),
            d.getCompagnieAerienne(),
            mode.replace("_", " "),
            phoneInfo,
            frontendUrl
        );
        send(d.getUser().getEmail(), d.getUser().getFirstName() + " " + d.getUser().getLastName(),
            "💳 Paiement reçu — Demande #" + d.getNumeroDemande() + " en cours de traitement", body);
    }

    // 7. Ticket issued → email to client
    @Async
    public void sendBilletClient(DemandeVol d) {
        String body = """
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Votre billet d'avion est prêt ! 🎉</p>
        <div style="background:#f0f9f0;border:2px solid #1a6b6b;border-radius:12px;padding:24px;margin:20px 0;text-align:center">
          <div style="font-size:13px;color:#888;text-transform:uppercase;letter-spacing:1px">N° Billet</div>
          <div style="font-size:28px;font-weight:900;color:#1a6b6b;letter-spacing:2px;margin:8px 0">%s</div>
          <div style="font-size:18px;font-weight:700;color:#333">%s → %s</div>
          <div style="color:#666;margin-top:4px">%s · %s</div>
          <div style="font-size:15px;font-weight:600;color:#333;margin-top:8px">%s</div>
        </div>
        <p>Présentez ce numéro de billet à l'aéroport ou contactez-nous pour recevoir votre e-ticket complet.</p>
        <p style="margin-top:20px;color:#888;font-size:13px">Bon voyage ! ✈️<br><strong>L'équipe YVAS</strong></p>
        """.formatted(
            d.getUser().getFirstName(),
            d.getNumeroBillet(),
            d.getOrigine(), d.getDestination(),
            d.getDateDepart().format(DATE),
            d.getDateRetour() != null ? "Retour: " + d.getDateRetour().format(DATE) : "Aller simple",
            d.getCompagnieAerienne()
        );
        send(d.getUser().getEmail(), d.getUser().getFirstName() + " " + d.getUser().getLastName(),
            "🎫 Votre billet #" + d.getNumeroBillet() + " — " + d.getOrigine() + " → " + d.getDestination(), body);
    }

    // ===== HELPERS =====

    private String detailsCard(DemandeVol d) {
        return """
        <div style="background:#f8f9fa;border-radius:10px;padding:20px;margin:16px 0;font-size:14px">
          <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #eee">
            <span style="color:#777">Référence</span><strong>%s</strong>
          </div>
          <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #eee">
            <span style="color:#777">Itinéraire</span><strong>%s → %s</strong>
          </div>
          <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #eee">
            <span style="color:#777">Départ</span><strong>%s</strong>
          </div>
          %s
          <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #eee">
            <span style="color:#777">Passagers</span><strong>%s</strong>
          </div>
          <div style="display:flex;justify-content:space-between;padding:8px 0">
            <span style="color:#777">Classe</span><strong>%s</strong>
          </div>
        </div>
        """.formatted(
            d.getNumeroDemande(),
            d.getOrigine(), d.getDestination(),
            d.getDateDepart().format(DATE),
            d.getDateRetour() != null
                ? "<div style='display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #eee'><span style='color:#777'>Retour</span><strong>" + d.getDateRetour().format(DATE) + "</strong></div>"
                : "",
            passengersLabel(d),
            d.getClasseVoyage()
        );
    }

    private String prixCard(DemandeVol d) {
        return """
        <div style="margin-top:12px;font-size:13px;text-align:left;display:inline-block">
          <div style="padding:4px 0"><span style="color:#777">Prix/personne : </span><strong>%s FCFA</strong></div>
          %s
        </div>
        """.formatted(
            formatPrice(d.getPrixParPersonne()),
            d.getNotesAdmin() != null ? "<div style='padding:4px 0;color:#555'>Note : " + d.getNotesAdmin() + "</div>" : ""
        );
    }

    private String passengersLabel(DemandeVol d) {
        StringBuilder sb = new StringBuilder();
        sb.append(d.getNbAdultes()).append(" adulte(s)");
        if (d.getNbEnfants() > 0) sb.append(", ").append(d.getNbEnfants()).append(" enfant(s)");
        if (d.getNbBebes() > 0) sb.append(", ").append(d.getNbBebes()).append(" bébé(s)");
        return sb.toString();
    }

    private String formatPrice(Double price) {
        if (price == null) return "—";
        return String.format("%,.0f", price).replace(",", " ");
    }

    private String wrap(String content) {
        return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><style>
        body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}
        .c{max-width:600px;margin:30px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)}
        .h{background:linear-gradient(135deg,#1a6b6b,#0d4040);padding:36px 30px;text-align:center;color:white}
        .h h1{margin:0;font-size:22px;font-weight:700}
        .b{padding:28px}
        .footer{background:#f8f8f8;padding:16px;text-align:center;color:#999;font-size:12px}
        </style></head><body><div class="c">
        <div class="h"><h1>✈️ YVAS — Yatou Voyages Assurances Services</h1></div>
        <div class="b">%s</div>
        <div class="footer">📍 Dakar, Sénégal · +221 78 143 44 44 · yvasvoye@gmail.com</div>
        </div></body></html>
        """.formatted(content);
    }

    private void send(String toEmail, String toName, String subject, String html) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Email non envoyé (clé Brevo manquante) à {} : {}", toEmail, subject);
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            Map<String, Object> body = Map.of(
                "sender",  Map.of("name", fromName, "email", fromEmail),
                "to",      List.of(Map.of("email", toEmail, "name", toName)),
                "subject", subject,
                "htmlContent", wrap(html)
            );
            ResponseEntity<String> response = rest.postForEntity(
                BREVO_API, new HttpEntity<>(body, headers), String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email vol envoyé à {} : {}", toEmail, subject);
            } else {
                log.error("Brevo erreur {} pour {} : {}", response.getStatusCode(), toEmail, response.getBody());
            }
        } catch (Exception e) {
            log.error("Email ECHEC pour {} : {}", toEmail, e.getMessage());
        }
    }
}
