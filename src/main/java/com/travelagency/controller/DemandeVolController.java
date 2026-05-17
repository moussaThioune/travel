package com.travelagency.controller;

import com.travelagency.dto.DemandeVolDTOs;
import com.travelagency.service.DemandeVolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes-vol")
@RequiredArgsConstructor
public class DemandeVolController {

    private final DemandeVolService service;

    // ===== CLIENT =====

    /** Submit a new flight request */
    @PostMapping
    public ResponseEntity<DemandeVolDTOs.Response> submit(
            @Valid @RequestBody DemandeVolDTOs.Request req) {
        return ResponseEntity.ok(service.submit(req));
    }

    /** Get my own requests */
    @GetMapping("/mes")
    public ResponseEntity<List<DemandeVolDTOs.Response>> getMesDemandes() {
        return ResponseEntity.ok(service.getMesDemandes());
    }

    /** Accept admin pricing */
    @PutMapping("/{id}/accepter")
    public ResponseEntity<DemandeVolDTOs.Response> accepter(@PathVariable Long id) {
        return ResponseEntity.ok(service.accepter(id));
    }

    /** Reject admin pricing */
    @PutMapping("/{id}/rejeter")
    public ResponseEntity<DemandeVolDTOs.Response> rejeter(@PathVariable Long id) {
        return ResponseEntity.ok(service.rejeter(id));
    }

    // ===== ADMIN =====

    /** Get all requests */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeVolDTOs.Response>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** Get specific request */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeVolDTOs.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /** Admin: respond with pricing */
    @PutMapping("/{id}/repondre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeVolDTOs.Response> repondre(
            @PathVariable Long id,
            @Valid @RequestBody DemandeVolDTOs.ReponseAdminRequest req) {
        return ResponseEntity.ok(service.repondre(id, req));
    }

    /** Admin: validate after client acceptance */
    @PutMapping("/{id}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeVolDTOs.Response> valider(@PathVariable Long id) {
        return ResponseEntity.ok(service.valider(id));
    }

    /** Admin: mark as paid */
    @PutMapping("/{id}/marquer-paye")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeVolDTOs.Response> marquerPaye(@PathVariable Long id) {
        return ResponseEntity.ok(service.marquerPaye(id));
    }

    /** Admin: issue ticket */
    @PutMapping("/{id}/emettre-ticket")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeVolDTOs.Response> emettreTicket(
            @PathVariable Long id,
            @Valid @RequestBody DemandeVolDTOs.BilletRequest req) {
        return ResponseEntity.ok(service.emettreTicket(id, req));
    }
}
