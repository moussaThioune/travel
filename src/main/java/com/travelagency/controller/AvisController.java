package com.travelagency.controller;

import com.travelagency.entity.Avis;
import com.travelagency.service.AvisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
public class AvisController {

    private final AvisService avisService;

    @GetMapping
    public ResponseEntity<List<Avis>> getAll() {
        return ResponseEntity.ok(avisService.findAll());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(avisService.getStats());
    }

    @PostMapping
    public ResponseEntity<Avis> create(@RequestBody Avis avis) {
        return ResponseEntity.ok(avisService.create(avis));
    }
}
