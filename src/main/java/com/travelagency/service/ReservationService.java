package com.travelagency.service;

import com.travelagency.dto.ReservationDTOs;
import com.travelagency.entity.Client;
import com.travelagency.entity.Reservation;
import com.travelagency.entity.Voyage;
import com.travelagency.repository.ClientRepository;
import com.travelagency.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final VoyageService voyageService;
    private final EmailService emailService;

    @Transactional
    public ReservationDTOs.Response createReservation(ReservationDTOs.Request request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        Voyage voyage = voyageService.findById(request.voyageId);
        if (!voyage.hasAvailablePlaces(request.nombrePersonnes)) {
            throw new RuntimeException("Pas assez de places disponibles: " + voyage.getNombrePlacesDisponibles());
        }
        BigDecimal prixTotal = voyage.getPrixParPersonne().multiply(BigDecimal.valueOf(request.nombrePersonnes));
        Reservation reservation = Reservation.builder()
                .numeroReservation(generateRef())
                .client(client).voyage(voyage)
                .nombrePersonnes(request.nombrePersonnes)
                .prixTotal(prixTotal)
                .statut(Reservation.StatutReservation.EN_ATTENTE)
                .dateReservation(LocalDateTime.now())
                .notes(request.notes).build();
        voyage.reduceAvailablePlaces(request.nombrePersonnes);
        if (voyage.getNombrePlacesDisponibles() == 0) voyage.setStatut(Voyage.StatutVoyage.COMPLET);
        reservation = reservationRepository.save(reservation);
        emailService.sendReservationConfirmation(reservation);
        return toResponse(reservation);
    }

    public List<ReservationDTOs.Response> getMyReservations() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        return reservationRepository.findByClientId(client.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<ReservationDTOs.Response> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public ReservationDTOs.Response getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ReservationDTOs.Response cancel(Long id) {
        Reservation reservation = findById(id);
        if (reservation.getStatut() == Reservation.StatutReservation.PAYEE) {
            throw new RuntimeException("Impossible d'annuler une réservation payée");
        }
        reservation.getVoyage().increaseAvailablePlaces(reservation.getNombrePersonnes());
        reservation.setStatut(Reservation.StatutReservation.ANNULEE);
        reservation = reservationRepository.save(reservation);
        emailService.sendReservationCancellation(reservation);
        return toResponse(reservation);
    }

    @Transactional
    public ReservationDTOs.Response updateStatut(Long id, Reservation.StatutReservation statut) {
        Reservation reservation = findById(id);
        reservation.setStatut(statut);
        return toResponse(reservationRepository.save(reservation));
    }

    // Appelé après validation d'un paiement Mobile Money
    @Transactional
    public void refreshReservation(Long id) {
        Reservation reservation = findById(id);
        // Recalculer montant payé basé sur les paiements SUCCES
        BigDecimal paid = reservation.getMontantPaye();
        if (paid.compareTo(reservation.getPrixTotal()) >= 0) {
            reservation.setStatut(Reservation.StatutReservation.PAYEE);
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            reservation.setStatut(Reservation.StatutReservation.CONFIRMEE);
        }
        reservationRepository.save(reservation);
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée: " + id));
    }

    private String generateRef() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public ReservationDTOs.Response toResponse(Reservation r) {
        ReservationDTOs.Response res = new ReservationDTOs.Response();
        res.id = r.getId();
        res.numeroReservation = r.getNumeroReservation();
        res.nombrePersonnes = r.getNombrePersonnes();
        res.prixTotal = r.getPrixTotal();
        res.statut = r.getStatut();
        res.dateReservation = r.getDateReservation();
        res.notes = r.getNotes();
        res.montantPaye = r.getMontantPaye();
        res.montantRestant = r.getMontantRestant();
        Client c = r.getClient();
        res.client = ReservationDTOs.ClientSummary.builder()
                .id(c.getId()).firstName(c.getFirstName()).lastName(c.getLastName())
                .email(c.getEmail()).phone(c.getPhone()).build();
        res.voyage = voyageService.toResponse(r.getVoyage());
        return res;
    }
}
