package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.location.entity.City;
import com.moviefixer.location.repository.CityRepository;
import com.moviefixer.theater.dto.CreateTheaterRequest;
import com.moviefixer.theater.dto.TheaterResponse;
import com.moviefixer.theater.dto.TheaterSummaryResponse;
import com.moviefixer.theater.dto.UpdateTheaterRequest;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.repository.ScreenRepository;
import com.moviefixer.theater.repository.TheaterRepository;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.USER_ROLE;
import com.moviefixer.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TheaterServiceImpl implements TheaterService {

    private static final Logger log = LoggerFactory.getLogger(TheaterServiceImpl.class);

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    public TheaterServiceImpl(
            TheaterRepository theaterRepository,
            ScreenRepository screenRepository,
            CityRepository cityRepository,
            UserRepository userRepository
    ) {
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public TheaterResponse createTheater(CreateTheaterRequest request, UserPrincipal currentUser) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));

        if (theaterRepository.existsByNameIgnoreCaseAndCityId(request.getName().trim(), city.getId())) {
            throw new IllegalArgumentException("Theater with name '" + request.getName() + "' already exists in " + city.getName());
        }

        UUID vendorId;
        if (isAdmin(currentUser) && request.getVendorId() != null) {
            vendorId = request.getVendorId();
        } else {
            vendorId = currentUser.getId();
        }

        Users vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", vendorId));

        boolean isAuthorizedVendor = vendor.getRoles().stream()
                .anyMatch(r -> r.getName() == USER_ROLE.THEATER_VENDOR || r.getName() == USER_ROLE.ADMIN);

        if (!isAuthorizedVendor) {
            throw new IllegalArgumentException("User " + vendor.getUsername() + " does not possess THEATER_VENDOR or ADMIN role");
        }

        Theater theater = new Theater();
        theater.setName(request.getName().trim());
        theater.setAddress(request.getAddress().trim());
        theater.setCity(city);
        theater.setVendor(vendor);
        theater.setContactPhone(request.getContactPhone());
        theater.setContactEmail(request.getContactEmail());
        theater.setLatitude(request.getLatitude());
        theater.setLongitude(request.getLongitude());
        theater.setActive(true);

        Theater saved = theaterRepository.save(theater);
        log.info("Successfully registered theater: {} [ID: {}] for vendor: {}", saved.getName(), saved.getId(), vendor.getUsername());
        return mapToResponse(saved, 0L);
    }

    @Override
    @Transactional
    public TheaterResponse updateTheater(UUID theaterId, UpdateTheaterRequest request, UserPrincipal currentUser) {
        Theater theater = theaterRepository.findByIdWithDetails(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", "id", theaterId));

        validateOwnership(theater, currentUser);

        if (request.getName() != null && !request.getName().isBlank()) {
            theater.setName(request.getName().trim());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            theater.setAddress(request.getAddress().trim());
        }
        if (request.getCityId() != null) {
            City newCity = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
            theater.setCity(newCity);
        }
        if (request.getContactPhone() != null) {
            theater.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            theater.setContactEmail(request.getContactEmail());
        }
        if (request.getLatitude() != null) {
            theater.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            theater.setLongitude(request.getLongitude());
        }
        if (request.getActive() != null) {
            theater.setActive(request.getActive());
        }

        Theater updated = theaterRepository.save(theater);
        long screenCount = screenRepository.countByTheaterIdAndActiveTrue(updated.getId());
        log.info("Updated theater ID: {} ({})", updated.getId(), updated.getName());
        return mapToResponse(updated, screenCount);
    }

    @Override
    @Transactional(readOnly = true)
    public TheaterResponse getTheaterById(UUID theaterId) {
        Theater theater = theaterRepository.findByIdWithDetails(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", "id", theaterId));
        long screenCount = screenRepository.countByTheaterIdAndActiveTrue(theater.getId());
        return mapToResponse(theater, screenCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterSummaryResponse> searchTheaters(UUID cityId, String keyword, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return theaterRepository.searchTheaters(cityId, cleanKeyword, pageable)
                .map(t -> {
                    long screenCount = screenRepository.countByTheaterIdAndActiveTrue(t.getId());
                    return new TheaterSummaryResponse(
                            t.getId(),
                            t.getName(),
                            t.getAddress(),
                            t.getCity().getName(),
                            t.getContactPhone(),
                            screenCount,
                            t.isActive()
                    );
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterResponse> getTheatersByVendor(UUID vendorId, Pageable pageable, UserPrincipal currentUser) {
        if (!isAdmin(currentUser) && !currentUser.getId().equals(vendorId)) {
            throw new AccessDeniedException("You do not have permission to view theaters for this vendor");
        }

        return theaterRepository.findByVendorId(vendorId, pageable)
                .map(t -> {
                    long screenCount = screenRepository.countByTheaterIdAndActiveTrue(t.getId());
                    return mapToResponse(t, screenCount);
                });
    }

    @Override
    @Transactional
    public void deleteTheater(UUID theaterId, UserPrincipal currentUser) {
        Theater theater = theaterRepository.findByIdWithDetails(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", "id", theaterId));

        validateOwnership(theater, currentUser);

        theater.setActive(false);
        theaterRepository.save(theater);
        log.info("Soft-archived theater ID: {} ({})", theater.getId(), theater.getName());
    }

    private void validateOwnership(Theater theater, UserPrincipal currentUser) {
        if (!isAdmin(currentUser) && !theater.getVendor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to modify theater " + theater.getName());
        }
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private TheaterResponse mapToResponse(Theater theater, long totalScreens) {
        City city = theater.getCity();
        Users vendor = theater.getVendor();

        return new TheaterResponse(
                theater.getId(),
                theater.getName(),
                theater.getAddress(),
                city.getId(),
                city.getName(),
                (city.getState() != null) ? city.getState().getName() : null,
                (city.getState() != null && city.getState().getCountry() != null) ? city.getState().getCountry().getName() : null,
                vendor.getId(),
                vendor.getName(),
                vendor.getEmail(),
                theater.getContactPhone(),
                theater.getContactEmail(),
                theater.getLatitude(),
                theater.getLongitude(),
                theater.isActive(),
                totalScreens,
                theater.getCreatedAt(),
                theater.getUpdatedAt()
        );
    }
}
