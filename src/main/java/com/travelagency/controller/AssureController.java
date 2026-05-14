package com.travelagency.controller;

import com.travelagency.dto.AssureDTOs;
import com.travelagency.service.AssureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assures")
@RequiredArgsConstructor
public class AssureController {

    private final AssureService service;

    // ===== CRUD =====
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AssureDTOs.Response> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AssureDTOs.Response getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AssureDTOs.Response create(@Valid @RequestBody AssureDTOs.Request req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AssureDTOs.Response update(@PathVariable Long id,
                                      @Valid @RequestBody AssureDTOs.Request req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== RECHERCHE & FILTRES =====
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AssureDTOs.Response> search(@RequestParam String q) { return service.search(q); }

    @GetMapping("/echeances")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AssureDTOs.Response> echeancesProches(
            @RequestParam(defaultValue = "30") int jours) {
        return service.getEcheancesProches(jours);
    }

    @GetMapping("/expires")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AssureDTOs.Response> expires() { return service.getExpires(); }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AssureDTOs.Stats stats() { return service.getStats(); }

    // ===== NOTIFICATIONS =====
    @PostMapping("/{id}/notifier")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> notifier(
            @PathVariable Long id,
            @RequestBody AssureDTOs.NotifManuelleRequest req) {
        String result = service.notifierManuellement(id, req);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/job/rappels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> lancerRappels() {
        service.envoyerRappelsMensuelsTous();
        return ResponseEntity.ok(Map.of("message", "Rappels lancés avec succès"));
    }
}
