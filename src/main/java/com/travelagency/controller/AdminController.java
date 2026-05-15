package com.travelagency.controller;

import com.travelagency.dto.PaiementDTOs;
import com.travelagency.dto.ReservationDTOs;
import com.travelagency.dto.VoyageDTOs;
import com.travelagency.entity.Reservation;
import com.travelagency.service.PaiementService;
import com.travelagency.service.ReservationService;
import com.travelagency.service.VoyageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final ReservationService reservationService;
    private final PaiementService paiementService;
    private final VoyageService voyageService;

    // ===== DASHBOARD =====
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        List<ReservationDTOs.Response> reservations = reservationService.getAllReservations();
        PaiementDTOs.Stats pmtStats = paiementService.getStats();
        List<VoyageDTOs.Response> voyages = voyageService.getAllVoyages();

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalVoyages", voyages.size());
        dashboard.put("voyagesActifs", voyages.stream().filter(v -> "ACTIF".equals(v.statut != null ? v.statut.name() : "")).count());
        dashboard.put("totalReservations", reservations.size());
        dashboard.put("reservationsEnAttente", reservations.stream().filter(r -> r.statut == Reservation.StatutReservation.EN_ATTENTE).count());
        dashboard.put("reservationsPayees", reservations.stream().filter(r -> r.statut == Reservation.StatutReservation.PAYEE).count());
        dashboard.put("totalClients", reservations.stream().map(r -> r.client.email).distinct().count());
        dashboard.put("paiementStats", pmtStats);
        dashboard.put("recentesReservations", reservations.subList(0, Math.min(8, reservations.size())));
        dashboard.put("mobileMoneyPending", paiementService.getMobileMoneyEnAttente().size());

        return ResponseEntity.ok(dashboard);
    }

    // ===== RESERVATIONS =====
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDTOs.Response>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationDTOs.Response> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    @PutMapping("/reservations/{id}/statut")
    public ResponseEntity<ReservationDTOs.Response> updateStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Reservation.StatutReservation statut = Reservation.StatutReservation.valueOf(body.get("statut"));
        return ResponseEntity.ok(reservationService.updateStatut(id, statut));
    }

    @PutMapping("/reservations/{id}/cancel")
    public ResponseEntity<ReservationDTOs.Response> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancel(id));
    }

    // ===== PAIEMENTS MOBILE MONEY =====
    @GetMapping("/paiements/pending")
    public ResponseEntity<List<PaiementDTOs.Response>> getPending() {
        return ResponseEntity.ok(paiementService.getMobileMoneyEnAttente());
    }

    @GetMapping("/paiements")
    public ResponseEntity<List<PaiementDTOs.Response>> getAllPaiements() {
        return ResponseEntity.ok(paiementService.getAll());
    }

    @PutMapping("/paiements/{id}/valider")
    public ResponseEntity<PaiementDTOs.Response> validerPaiement(@PathVariable Long id) {
        return ResponseEntity.ok(paiementService.validerPaiement(id));
    }

    @PutMapping("/paiements/{id}/rejeter")
    public ResponseEntity<PaiementDTOs.Response> rejeterPaiement(
            @PathVariable Long id,
            @RequestBody PaiementDTOs.ValidationRequest body) {
        return ResponseEntity.ok(paiementService.rejeterPaiement(id, body.reason));
    }

    @GetMapping("/paiements/stats")
    public ResponseEntity<PaiementDTOs.Stats> getPaiementStats() {
        return ResponseEntity.ok(paiementService.getStats());
    }

    // ===== VOYAGES =====
    @GetMapping("/voyages")
    public ResponseEntity<List<VoyageDTOs.Response>> getAllVoyages() {
        return ResponseEntity.ok(voyageService.getAllVoyages());
    }

    @PostMapping("/voyages")
    public ResponseEntity<VoyageDTOs.Response> createVoyage(
            @RequestBody VoyageDTOs.CreateRequest request) {
        return ResponseEntity.ok(voyageService.create(request));
    }

    @PutMapping("/voyages/{id}")
    public ResponseEntity<VoyageDTOs.Response> updateVoyage(
            @PathVariable Long id,
            @RequestBody VoyageDTOs.CreateRequest request) {
        return ResponseEntity.ok(voyageService.update(id, request));
    }

    @DeleteMapping("/voyages/{id}")
    public ResponseEntity<Void> deleteVoyage(@PathVariable Long id) {
        voyageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
