package com.moviefixer.common.initializer;

import com.moviefixer.location.entity.City;
import com.moviefixer.location.entity.Country;
import com.moviefixer.location.entity.State;
import com.moviefixer.location.repository.CityRepository;
import com.moviefixer.location.repository.CountryRepository;
import com.moviefixer.location.repository.StateRepository;
import com.moviefixer.users.entity.Role;
import com.moviefixer.users.enums.USER_ROLE;
import com.moviefixer.movie.entity.Genre;
import com.moviefixer.movie.entity.Language;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.enums.TierType;
import com.moviefixer.users.repository.RoleRepository;
import com.moviefixer.movie.repository.GenreRepository;
import com.moviefixer.movie.repository.LanguageRepository;
import com.moviefixer.theater.repository.SeatingTierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final RoleRepository roleRepository;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final SeatingTierRepository seatingTierRepository;

    public DatabaseSeeder(
            RoleRepository roleRepository,
            CountryRepository countryRepository,
            StateRepository stateRepository,
            CityRepository cityRepository,
            GenreRepository genreRepository,
            LanguageRepository languageRepository,
            SeatingTierRepository seatingTierRepository) {
        this.roleRepository = roleRepository;
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.seatingTierRepository = seatingTierRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedGeographicData();
        seedMovieCatalogMetadata();
        seedSeatingTiers();
    }

    private void seedRoles() {
        for (USER_ROLE roleEnum : USER_ROLE.values()) {
            if (!roleRepository.existsByName(roleEnum)) {
                String description = switch (roleEnum) {
                    case CUSTOMER -> "Standard customer for movie ticket booking";
                    case ADMIN -> "Platform administrator with full privileges";
                    case THEATER_VENDOR -> "Theater vendor / owner managing multiple screens";
                    case THEATER_MANAGER -> "Theater manager handling day-to-day operations";
                    case THEATER_TICKET_STAFF -> "Theater ticketing staff for scanning and box office";
                    case ADMIN_SUPPORT -> "Customer support representative";
                };
                roleRepository.save(new Role(roleEnum, description));
                log.info("Seeded role: {}", roleEnum);
            }
        }
    }

    private void seedGeographicData() {
        if (countryRepository.count() == 0) {
            log.info("Seeding initial geographic reference data (countries, states, cities)...");

            // 1. India
            Country india = countryRepository.save(new Country("IN", "India", "+91"));

            // States for India
            State maharashtra = stateRepository.save(new State("Maharashtra", "MH", india));
            State karnataka = stateRepository.save(new State("Karnataka", "KA", india));
            State delhi = stateRepository.save(new State("Delhi", "DL", india));
            State telangana = stateRepository.save(new State("Telangana", "TG", india));
            State tamilNadu = stateRepository.save(new State("Tamil Nadu", "TN", india));

            // Cities for India
            cityRepository.saveAll(List.of(
                    new City("Mumbai", maharashtra),
                    new City("Pune", maharashtra),
                    new City("Nagpur", maharashtra),
                    new City("Bengaluru", karnataka),
                    new City("Mysuru", karnataka),
                    new City("New Delhi", delhi),
                    new City("Hyderabad", telangana),
                    new City("Chennai", tamilNadu)
            ));

            // 2. United States
            Country usa = countryRepository.save(new Country("US", "United States", "+1"));

            // States for USA
            State california = stateRepository.save(new State("California", "CA", usa));
            State newYork = stateRepository.save(new State("New York", "NY", usa));
            State texas = stateRepository.save(new State("Texas", "TX", usa));

            // Cities for USA
            cityRepository.saveAll(List.of(
                    new City("Los Angeles", california),
                    new City("San Francisco", california),
                    new City("New York City", newYork),
                    new City("Austin", texas),
                    new City("Dallas", texas)
            ));

            log.info("Geographic reference data seeding completed successfully.");
        }
    }

    private void seedMovieCatalogMetadata() {
        if (genreRepository.count() == 0) {
            log.info("Seeding initial movie genres...");
            List<Genre> genres = List.of(
                    new Genre("Action", "ACTION", "High energy, combat, stunts, and physical feats"),
                    new Genre("Comedy", "COMEDY", "Humorous and entertaining themes"),
                    new Genre("Drama", "DRAMA", "In-depth character development and emotional narratives"),
                    new Genre("Sci-Fi", "SCI_FI", "Futuristic science and technological themes"),
                    new Genre("Horror", "HORROR", "Suspense, terror, and supernatural thrills"),
                    new Genre("Romance", "ROMANCE", "Love stories and intimate relationships"),
                    new Genre("Thriller", "THRILLER", "High-suspense, psychological twists, and excitement"),
                    new Genre("Animation", "ANIMATION", "Animated and CGI cinematic features"),
                    new Genre("Adventure", "ADVENTURE", "Exciting journeys and heroic quests"),
                    new Genre("Fantasy", "FANTASY", "Magic, mythical beings, and enchanted realms"),
                    new Genre("Crime", "CRIME", "Criminal investigations, mystery, and mob stories")
            );
            genreRepository.saveAll(genres);
            log.info("Movie genres seeding completed.");
        }

        if (languageRepository.count() == 0) {
            log.info("Seeding initial movie screening languages...");
            List<Language> languages = List.of(
                    new Language("English", "en"),
                    new Language("Hindi", "hi"),
                    new Language("Telugu", "te"),
                    new Language("Tamil", "ta"),
                    new Language("Malayalam", "ml"),
                    new Language("Kannada", "kn"),
                    new Language("Spanish", "es")
            );
            languageRepository.saveAll(languages);
            log.info("Movie screening languages seeding completed.");
        }
    }

    private void seedSeatingTiers() {
        if (seatingTierRepository.count() == 0) {
            log.info("Seeding standard seating tiers (VIP, Premium, Executive, Standard)...");
            List<SeatingTier> tiers = List.of(
                    new SeatingTier("VIP", TierType.VIP, new java.math.BigDecimal("2.00"), "Luxury recliners with prime sightline and maximum legroom", true),
                    new SeatingTier("Premium", TierType.PREMIUM, new java.math.BigDecimal("1.50"), "Elevated viewing tier with extra comfort and wider seating", true),
                    new SeatingTier("Executive", TierType.EXECUTIVE, new java.math.BigDecimal("1.20"), "Standard cinema seating with optimal sound and screen view", true),
                    new SeatingTier("Standard", TierType.STANDARD, new java.math.BigDecimal("1.00"), "Value admission seating", true)
            );
            seatingTierRepository.saveAll(tiers);
            log.info("Seating tiers seeding completed.");
        }
    }
}
