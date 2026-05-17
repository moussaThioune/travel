package com.travelagency.controller;

import com.travelagency.service.AssureSmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SmsController {

    private final AssureSmsService smsService;

    /**
     * Envoyer un SMS de test via Twilio.
     *
     * POST /api/sms/test?telephone=+221XXXXXXXXX
     * POST /api/sms/test?telephone=+221XXXXXXXXX&message=Votre message ici
     *
     * Header: Authorization: Bearer <token_admin>
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testSms(
            @RequestParam String telephone,
            @RequestParam(required = false) String message) {

        smsService.sendTestSms(telephone, message);

        return ResponseEntity.ok(Map.of(
            "status", "SMS envoyé — vérifiez les logs Railway",
            "telephone", telephone,
            "message", message != null ? message : "✅ Test SMS Twilio — Yatou Voyage fonctionne !"
        ));
    }
}
