package com.travelagency.dto;

import com.travelagency.entity.DemandeVol;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DemandeVolDTOs {

    @Data
    public static class Request {
        @NotBlank private String origine;
        @NotBlank private String destination;
        @NotNull  private LocalDate dateDepart;
        private LocalDate dateRetour;
        @NotBlank private String typeVoyage;
        @Min(1)   private int nbAdultes;
        private int nbEnfants;
        private int nbBebes;
        @NotBlank private String classeVoyage;
        private boolean volsDirects;
        private boolean aeroportsProximiteOrigine;
        private boolean aeroportsProximiteDestination;
        private String notesClient;
    }

    @Data
    public static class ReponseAdminRequest {
        @NotBlank private String compagnieAerienne;
        @NotNull  private Double prixParPersonne;
        @NotNull  private Double prixTotal;
        private String dureeVol;
        private String escales;
        private String notesAdmin;
    }

    @Data
    public static class BilletRequest {
        @NotBlank private String numeroBillet;
        private String notesAdmin;
    }

    @Data
    public static class Response {
        private Long id;
        private String numeroDemande;
        private String clientNom;
        private String clientEmail;
        private String origine;
        private String destination;
        private LocalDate dateDepart;
        private LocalDate dateRetour;
        private String typeVoyage;
        private int nbAdultes;
        private int nbEnfants;
        private int nbBebes;
        private int totalPassagers;
        private String classeVoyage;
        private boolean volsDirects;
        private String notesClient;
        private String compagnieAerienne;
        private Double prixParPersonne;
        private Double prixTotal;
        private String dureeVol;
        private String escales;
        private String notesAdmin;
        private String numeroBillet;
        private DemandeVol.Statut statut;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(DemandeVol d) {
            Response r = new Response();
            r.id = d.getId();
            r.numeroDemande = d.getNumeroDemande();
            r.clientNom = d.getUser().getFirstName() + " " + d.getUser().getLastName();
            r.clientEmail = d.getUser().getEmail();
            r.origine = d.getOrigine();
            r.destination = d.getDestination();
            r.dateDepart = d.getDateDepart();
            r.dateRetour = d.getDateRetour();
            r.typeVoyage = d.getTypeVoyage();
            r.nbAdultes = d.getNbAdultes();
            r.nbEnfants = d.getNbEnfants();
            r.nbBebes = d.getNbBebes();
            r.totalPassagers = d.totalPassagers();
            r.classeVoyage = d.getClasseVoyage();
            r.volsDirects = d.isVolsDirects();
            r.notesClient = d.getNotesClient();
            r.compagnieAerienne = d.getCompagnieAerienne();
            r.prixParPersonne = d.getPrixParPersonne();
            r.prixTotal = d.getPrixTotal();
            r.dureeVol = d.getDureeVol();
            r.escales = d.getEscales();
            r.notesAdmin = d.getNotesAdmin();
            r.numeroBillet = d.getNumeroBillet();
            r.statut = d.getStatut();
            r.createdAt = d.getCreatedAt();
            r.updatedAt = d.getUpdatedAt();
            return r;
        }
    }
}
