package com.travelagency.controller;

import com.travelagency.dto.VoyageDTOs;
import com.travelagency.service.VoyageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/voyages")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VoyageController {

    private final VoyageService voyageService;

    @GetMapping
    public ResponseEntity<List<VoyageDTOs.Response>> getAll() {
        return ResponseEntity.ok(voyageService.getAllVoyages());
    }

    @GetMapping("/available")
    public ResponseEntity<List<VoyageDTOs.Response>> getAvailable() {
        return ResponseEntity.ok(voyageService.getAvailableVoyages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoyageDTOs.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(voyageService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VoyageDTOs.Response>> search(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Integer places) {
        return ResponseEntity.ok(voyageService.search(destination, dateDepart, prixMax, places));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoyageDTOs.Response> create(@Valid @RequestBody VoyageDTOs.CreateRequest request) {
        return ResponseEntity.ok(voyageService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoyageDTOs.Response> update(@PathVariable Long id,
                                                       @Valid @RequestBody VoyageDTOs.CreateRequest request) {
        return ResponseEntity.ok(voyageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voyageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
