package com.travelagency.controller;

import com.travelagency.dto.PaiementDTOs;
import com.travelagency.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PaiementController {

    private final PaiementService paiementService;

    // Client: initier un paiement
    @PostMapping
    public ResponseEntity<PaiementDTOs.Response> process(
            @Valid @RequestBody PaiementDTOs.Request request) {
        return ResponseEntity.ok(paiementService.processPaiement(request));
    }

    // Client: voir paiements d'une réservation
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaiementDTOs.Response>> getByReservation(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(paiementService.getByReservation(reservationId));
    }

    // Admin: tous les paiements
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaiementDTOs.Response>> getAll() {
        return ResponseEntity.ok(paiementService.getAll());
    }

    // Admin: paiements Mobile Money en attente
    @GetMapping("/mobile-money/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaiementDTOs.Response>> getMobileMoneyPending() {
        return ResponseEntity.ok(paiementService.getMobileMoneyEnAttente());
    }

    // Admin: tous les paiements Mobile Money (historique)
    @GetMapping("/mobile-money")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaiementDTOs.Response>> getAllMobileMoney() {
        return ResponseEntity.ok(paiementService.getAllMobileMoney());
    }

    // Admin: valider un paiement Mobile Money
    @PutMapping("/{id}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementDTOs.Response> valider(@PathVariable Long id) {
        return ResponseEntity.ok(paiementService.validerPaiement(id));
    }

    // Admin: rejeter un paiement Mobile Money
    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementDTOs.Response> rejeter(
            @PathVariable Long id,
            @RequestBody PaiementDTOs.ValidationRequest body) {
        return ResponseEntity.ok(paiementService.rejeterPaiement(id, body.reason));
    }

    // Admin: statistiques
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementDTOs.Stats> getStats() {
        return ResponseEntity.ok(paiementService.getStats());
    }
}
