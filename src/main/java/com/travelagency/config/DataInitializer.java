package com.travelagency.config;
import com.travelagency.entity.*;
import com.travelagency.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;



@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final VoyageRepository voyageRepository;
    private final AssureRepository assureRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("Initialisation des données de démo...");

        User admin = User.builder()
                .firstName("Admin").lastName("Travel")
                .email("admin@travelagency.com")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN).enabled(true).build();
        userRepository.save(admin);

        User clientUser = User.builder()
                .firstName("Jean").lastName("Dupont")
                .email("jean.dupont@email.com")
                .password(passwordEncoder.encode("client123"))
                .role(User.Role.CLIENT).enabled(true).build();
        userRepository.save(clientUser);

        Client client = Client.builder()
                .firstName("Jean").lastName("Dupont")
                .email("jean.dupont@email.com")
                .phone("+33 6 12 34 56 78")
                .address("123 Rue de la Paix").city("Paris").country("France")
                .passportNumber("FR123456")
                .user(clientUser).build();
        clientRepository.save(client);

        // ===== VOYAGES YVAS =====
        List<Voyage> voyages = List.of(
            // OMRA
            Voyage.builder()
                .titre("Omra Ramadan 2026 — 15 Derniers Jours")
                .description("Vivez les 15 derniers jours du Ramadan à La Mecque. Package complet incluant visa Omra, billet aller-retour, hébergement, restauration, visite Rawda, Khoulou, Ziarra Makkah-Médina et guidage religieux.")
                .destination("La Mecque / Médine").paysDestination("Arabie Saoudite")
                .dateDepart(LocalDate.of(2026, 3, 5))
                .dateRetour(LocalDate.of(2026, 3, 19))
                .prixParPersonne(new BigDecimal("2750000"))
                .nombrePlacesTotal(50).nombrePlacesDisponibles(20)
                .imageUrl("https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=1200")
                .categorie("OMRA").statut(Voyage.StatutVoyage.ACTIF).build(),

            Voyage.builder()
                .titre("Omra Ramadan 2026 — Package VIP")
                .description("Package VIP pour les 15 derniers jours du Ramadan. Hébergement 5 étoiles à proximité de la Kaaba, transport privatisé, guide personnel et services premium.")
                .destination("La Mecque / Médine").paysDestination("Arabie Saoudite")
                .dateDepart(LocalDate.of(2026, 3, 5))
                .dateRetour(LocalDate.of(2026, 3, 19))
                .prixParPersonne(new BigDecimal("3250000"))
                .nombrePlacesTotal(20).nombrePlacesDisponibles(8)
                .imageUrl("https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=1200")
                .categorie("OMRA").statut(Voyage.StatutVoyage.ACTIF).build(),

            // HADJ
            Voyage.builder()
                .titre("Hajj 2026 — Package Prestige 17 Jours")
                .description("Offrez-vous le voyage spirituel de votre vie avec YVAS. En partenariat avec Groupe UNACOIS JAPPO. Encadrement religieux professionnel, hébergement proche des lieux saints, transport et logistique assurés, accompagnement médical et administratif.")
                .destination("La Mecque / Médine").paysDestination("Arabie Saoudite")
                .dateDepart(LocalDate.of(2026, 6, 1))
                .dateRetour(LocalDate.of(2026, 6, 17))
                .prixParPersonne(new BigDecimal("6000000"))
                .nombrePlacesTotal(40).nombrePlacesDisponibles(15)
                .imageUrl("https://images.unsplash.com/photo-1537621547307-9b9f3b4b7e5f?w=1200")
                .categorie("HADJ").statut(Voyage.StatutVoyage.ACTIF).build(),

            Voyage.builder()
                .titre("Hajj 2026 — Package Standard 21 Jours")
                .description("Partez en toute sérénité vers les lieux saints. Package standard avec encadrement religieux, hébergement confortable et transport assuré. Visa + billets inclus.")
                .destination("La Mecque / Médine").paysDestination("Arabie Saoudite")
                .dateDepart(LocalDate.of(2026, 5, 25))
                .dateRetour(LocalDate.of(2026, 6, 14))
                .prixParPersonne(new BigDecimal("5100000"))
                .nombrePlacesTotal(60).nombrePlacesDisponibles(30)
                .imageUrl("https://images.unsplash.com/photo-1537621547307-9b9f3b4b7e5f?w=1200")
                .categorie("HADJ").statut(Voyage.StatutVoyage.ACTIF).build(),

            // ZIARRA
            Voyage.builder()
                .titre("Ziarra Fès — Du 13 au 20 Février")
                .description("Ziarra spirituelle à Fès, Maroc. Inclus: billet d'avion, hébergement, restauration, transport et transfert, encadrement religieux, Salats, Wazifa, Hadratul Jummah et Ziarras. Pièces requises: copie pièce d'identité + passeport en cours de validité.")
                .destination("Fès").paysDestination("Maroc")
                .dateDepart(LocalDate.of(2026, 2, 13))
                .dateRetour(LocalDate.of(2026, 2, 20))
                .prixParPersonne(new BigDecimal("800000"))
                .nombrePlacesTotal(30).nombrePlacesDisponibles(12)
                .imageUrl("https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=1200")
                .categorie("ZIARRA").statut(Voyage.StatutVoyage.ACTIF).build(),

            // COLONIES
            Voyage.builder()
                .titre("Colonie de Vacances — Cap Skirring 2026")
                .description("Colonie de vacances pour enfants à Cap Skirring du 20 Juillet au 4 Août. Plage, activités ludiques et éducatives dans un cadre naturel exceptionnel en Casamance.")
                .destination("Cap Skirring").paysDestination("Sénégal")
                .dateDepart(LocalDate.of(2026, 7, 20))
                .dateRetour(LocalDate.of(2026, 8, 4))
                .prixParPersonne(new BigDecimal("850000"))
                .nombrePlacesTotal(40).nombrePlacesDisponibles(28)
                .imageUrl("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200")
                .categorie("COLONIE").statut(Voyage.StatutVoyage.ACTIF).build(),

            Voyage.builder()
                .titre("Colonie de Vacances — Maroc 2026")
                .description("Colonie de vacances au Maroc du 10 au 24 Août. Découverte de la culture marocaine, visite des villes impériales et activités culturelles enrichissantes pour les jeunes.")
                .destination("Maroc").paysDestination("Maroc")
                .dateDepart(LocalDate.of(2026, 8, 10))
                .dateRetour(LocalDate.of(2026, 8, 24))
                .prixParPersonne(new BigDecimal("1200000"))
                .nombrePlacesTotal(30).nombrePlacesDisponibles(22)
                .imageUrl("https://images.unsplash.com/photo-1548013146-72479768bada?w=1200")
                .categorie("COLONIE").statut(Voyage.StatutVoyage.ACTIF).build()
        );
        voyageRepository.saveAll(voyages);
        log.info("✅ {} voyages YVAS créés", voyages.size());

        // ===== ASSURÉS DE DÉMO =====
        LocalDate today = LocalDate.now();
        List<Assure> assures = List.of(
            Assure.builder().nom("NDIAYE").prenom("MALICK").marque("FORD").immatricule("AA-858-PC")
                .puissanceFiscale("11CV").carburant("ESSENCE").numeroPolicce("5102023000326/002")
                .echeance(today.plusDays(7)).dateRappel(today).telephone("77655 62 43")
                .statut(Assure.StatutAssure.ACTIF).notes("Rappel urgent").build(),
            Assure.builder().nom("NIASSE").prenom("ABDOULAYE").marque("MERCEDES BENZ").immatricule("ZG-3821-C")
                .puissanceFiscale("09CV").carburant("GAZOLE").numeroPolicce("419PVD52024000215/000")
                .echeance(today.plusDays(12)).dateRappel(today.plusDays(5)).telephone("77540 84 84")
                .statut(Assure.StatutAssure.ACTIF).build(),
            Assure.builder().nom("DIOP").prenom("BARA").marque("FORD").immatricule("AA-918-PE")
                .puissanceFiscale("17CV").carburant("ESSENCE").numeroPolicce("419PDV5102025001268/000")
                .echeance(today.plusDays(30)).dateRappel(today.plusDays(23)).telephone("77634 81 54")
                .statut(Assure.StatutAssure.ACTIF).build(),
            Assure.builder().nom("DIONE").prenom("MARIETOU").marque("MERCEDES-BENZ").immatricule("DK-6745-S")
                .puissanceFiscale("8CV").carburant("GAZOLE").numeroPolicce("5102024002748/000")
                .echeance(today.plusDays(45)).dateRappel(today.plusDays(38)).telephone("77526 53 62")
                .statut(Assure.StatutAssure.ACTIF).build(),
            Assure.builder().nom("SEYE").prenom("AZIZ").marque("PEUGEOT").immatricule("AA-007-VB")
                .puissanceFiscale("6CV").numeroPolicce("419PDV5102025000962/001")
                .echeance(today.plusDays(60)).dateRappel(today.plusDays(53)).telephone("777788153")
                .statut(Assure.StatutAssure.ACTIF).build(),
            Assure.builder().nom("FALL").prenom("YACINE").marque("PEUGEOT").immatricule("AA-204-MJ")
                .puissanceFiscale("6CV").carburant("GAZOLE").numeroPolicce("419PVD5102025001343/000")
                .echeance(today.plusDays(90)).dateRappel(today.plusDays(83)).telephone("77266 59 77")
                .statut(Assure.StatutAssure.ACTIF).notes("ASTV").build(),
            Assure.builder().nom("TRAORE").prenom("MALICK").marque("MAZDA").immatricule("G0643274")
                .puissanceFiscale("14CV").carburant("ESSENCE").numeroPolicce("419PVD5102025002653/000")
                .echeance(today.plusDays(120)).dateRappel(today.plusDays(113)).telephone("773369380")
                .statut(Assure.StatutAssure.ACTIF).build(),
            Assure.builder().nom("GNINGUE").prenom("NDEYE MAREME").marque("PEUGEOT").immatricule("AA-793-RC")
                .puissanceFiscale("8CV").carburant("GAZOLE").numeroPolicce("419PVD5102024003150/000")
                .echeance(today.minusDays(5)).dateRappel(today.minusDays(12)).telephone("77 682 65 23")
                .statut(Assure.StatutAssure.EXPIRE).build(),
            Assure.builder().nom("HOTTE").prenom("DJIBY").marque("RENAULT").immatricule("AA-612-NB")
                .puissanceFiscale("6CV").carburant("GAZOLE")
                .echeance(today.minusDays(30)).dateRappel(today.minusDays(37)).telephone("77 281 47 81")
                .statut(Assure.StatutAssure.VENDU).notes("vendue").build(),
            Assure.builder().nom("MBAYE SALIOU").marque("FORD").immatricule("AA-116-WA")
                .puissanceFiscale("11CV").carburant("ESSENCE").numeroPolicce("419PVD5102025002589/000")
                .echeance(today.plusDays(180)).dateRappel(today.plusDays(173)).telephone("77 000 00 00")
                .statut(Assure.StatutAssure.ACTIF).build()
        );
        assureRepository.saveAll(assures);
        log.info("✅ {} assurés de démo créés", assures.size());
    }
}
