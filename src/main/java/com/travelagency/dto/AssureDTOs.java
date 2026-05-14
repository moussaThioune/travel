package com.travelagency.dto;

import com.travelagency.entity.Assure;
import com.travelagency.entity.NotificationAssure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AssureDTOs {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        @NotBlank public String nom;
        public String prenom;
        public String marque;
        public String immatricule;
        public String puissanceFiscale;
        public String carburant;
        public String numeroPolicce;
        public BigDecimal montantPrime;
        @NotNull public LocalDate echeance;
        public LocalDate dateRappel;
        public LocalDate periodeGarantieDebut;
        public Integer periodeGarantieAns;
        @NotBlank public String telephone;
        public String telephone2;
        public String email;
        public Assure.StatutAssure statut;
        public String notes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        public Long id;
        public String nom;
        public String prenom;
        public String nomComplet;
        public String marque;
        public String immatricule;
        public String puissanceFiscale;
        public String carburant;
        public String numeroPolicce;
        public BigDecimal montantPrime;
        public LocalDate echeance;
        public LocalDate dateRappel;
        public LocalDate periodeGarantieDebut;
        public Integer periodeGarantieAns;
        public String telephone;
        public String telephone2;
        public String email;
        public Assure.StatutAssure statut;
        public String notes;
        public boolean echeanceProche;
        public boolean expire;
        public long joursRestants;
        public LocalDateTime createdAt;
        public List<NotifResponse> notifications;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NotifResponse {
        public Long id;
        public NotificationAssure.TypeNotif type;
        public boolean succes;
        public String message;
        public LocalDateTime envoyeAt;
        public String erreur;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Stats {
        public long total;
        public long actifs;
        public long echeancesProches; // dans les 30 jours
        public long expires;
        public long vendus;
        public List<MoisStat> parMois;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MoisStat {
        public int mois;
        public String moisLabel;
        public long count;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NotifManuelleRequest {
        public boolean email;
        public boolean sms;
        public String messagePersonnalise;
    }
}
