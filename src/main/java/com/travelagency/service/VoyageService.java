package com.travelagency.service;

import com.travelagency.dto.VoyageDTOs;
import com.travelagency.entity.Hotel;
import com.travelagency.entity.Voyage;
import com.travelagency.entity.Vol;
import com.travelagency.repository.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoyageService {

    private final VoyageRepository voyageRepository;
    private final EntityManager entityManager;

    public List<VoyageDTOs.Response> getAllVoyages() {
        return voyageRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<VoyageDTOs.Response> getAvailableVoyages() {
        return voyageRepository.findAvailableVoyages().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public VoyageDTOs.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public List<VoyageDTOs.Response> search(String destination, LocalDate dateDepart,
                                              BigDecimal prixMax, Integer places) {
        return voyageRepository.searchVoyages(destination, dateDepart, prixMax, places)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public VoyageDTOs.Response create(VoyageDTOs.CreateRequest request) {
        Voyage voyage = Voyage.builder()
                .titre(request.titre)
                .description(request.description)
                .destination(request.destination)
                .paysDestination(request.paysDestination)
                .dateDepart(request.dateDepart)
                .dateRetour(request.dateRetour)
                .prixParPersonne(request.prixParPersonne)
                .nombrePlacesTotal(request.nombrePlacesTotal)
                .nombrePlacesDisponibles(request.nombrePlacesTotal)
                .imageUrl(request.imageUrl)
                .categorie(request.categorie)
                .statut(Voyage.StatutVoyage.ACTIF)
                .build();

        if (request.hotelId != null) {
            voyage.setHotel(entityManager.getReference(Hotel.class, request.hotelId));
        }
        if (request.volAllerId != null) {
            voyage.setVolAller(entityManager.getReference(Vol.class, request.volAllerId));
        }
        if (request.volRetourId != null) {
            voyage.setVolRetour(entityManager.getReference(Vol.class, request.volRetourId));
        }

        return toResponse(voyageRepository.save(voyage));
    }

    @Transactional
    public VoyageDTOs.Response update(Long id, VoyageDTOs.CreateRequest request) {
        Voyage voyage = findById(id);
        voyage.setTitre(request.titre);
        voyage.setDescription(request.description);
        voyage.setDestination(request.destination);
        voyage.setPaysDestination(request.paysDestination);
        voyage.setDateDepart(request.dateDepart);
        voyage.setDateRetour(request.dateRetour);
        voyage.setPrixParPersonne(request.prixParPersonne);
        voyage.setImageUrl(request.imageUrl);
        voyage.setCategorie(request.categorie);
        return toResponse(voyageRepository.save(voyage));
    }

    @Transactional
    public void delete(Long id) {
        voyageRepository.deleteById(id);
    }

    public Voyage findById(Long id) {
        return voyageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voyage non trouvé: " + id));
    }

    public VoyageDTOs.Response toResponse(Voyage v) {
        VoyageDTOs.Response r = new VoyageDTOs.Response();
        r.id = v.getId();
        r.titre = v.getTitre();
        r.description = v.getDescription();
        r.destination = v.getDestination();
        r.paysDestination = v.getPaysDestination();
        r.dateDepart = v.getDateDepart();
        r.dateRetour = v.getDateRetour();
        r.prixParPersonne = v.getPrixParPersonne();
        r.nombrePlacesTotal = v.getNombrePlacesTotal();
        r.nombrePlacesDisponibles = v.getNombrePlacesDisponibles();
        r.imageUrl = v.getImageUrl();
        r.categorie = v.getCategorie();
        r.statut = v.getStatut();
        r.dureeJours = v.getDureeJours();

        if (v.getHotel() != null) {
            Hotel h = v.getHotel();
            r.hotel = VoyageDTOs.HotelSummary.builder()
                    .id(h.getId()).nom(h.getNom()).ville(h.getVille())
                    .pays(h.getPays()).etoiles(h.getEtoiles()).imageUrl(h.getImageUrl()).build();
        }
        if (v.getVolAller() != null) {
            Vol vol = v.getVolAller();
            r.volAller = VoyageDTOs.VolSummary.builder()
                    .id(vol.getId()).numeroVol(vol.getNumeroVol()).compagnie(vol.getCompagnie())
                    .villeDepart(vol.getVilleDepart()).villeArrivee(vol.getVilleArrivee())
                    .dateDepart(vol.getDateDepart()).dateArrivee(vol.getDateArrivee()).build();
        }
        return r;
    }
}
