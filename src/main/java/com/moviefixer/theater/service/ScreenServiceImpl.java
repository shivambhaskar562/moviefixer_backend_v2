package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.theater.dto.CreateScreenRequest;
import com.moviefixer.theater.dto.ScreenResponse;
import com.moviefixer.theater.dto.UpdateScreenRequest;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.enums.ScreenType;
import com.moviefixer.theater.repository.ScreenRepository;
import com.moviefixer.theater.repository.TheaterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ScreenServiceImpl implements ScreenService {

    private static final Logger log = LoggerFactory.getLogger(ScreenServiceImpl.class);

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;

    public ScreenServiceImpl(ScreenRepository screenRepository, TheaterRepository theaterRepository) {
        this.screenRepository = screenRepository;
        this.theaterRepository = theaterRepository;
    }

    @Override
    @Transactional
    public ScreenResponse createScreen(UUID theaterId, CreateScreenRequest request, UserPrincipal currentUser) {
        Theater theater = theaterRepository.findByIdWithDetails(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", "id", theaterId));

        validateOwnership(theater, currentUser);

        if (screenRepository.existsByTheaterIdAndNameIgnoreCase(theaterId, request.getName().trim())) {
            throw new IllegalArgumentException("Screen with name '" + request.getName() + "' already exists in theater " + theater.getName());
        }

        Screen screen = new Screen();
        screen.setTheater(theater);
        screen.setName(request.getName().trim());
        screen.setScreenType(request.getScreenType() != null ? request.getScreenType() : ScreenType.STANDARD_2D);
        screen.setSoundSystem(request.getSoundSystem() != null ? request.getSoundSystem().trim() : "Dolby Atmos 7.1");
        screen.setTotalSeats(request.getTotalSeats() != null ? request.getTotalSeats() : 0);
        screen.setActive(true);

        Screen saved = screenRepository.save(screen);
        log.info("Registered screen: {} [ID: {}] in theater: {}", saved.getName(), saved.getId(), theater.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ScreenResponse updateScreen(UUID screenId, UpdateScreenRequest request, UserPrincipal currentUser) {
        Screen screen = screenRepository.findByIdWithTheater(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", screenId));

        validateOwnership(screen.getTheater(), currentUser);

        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = request.getName().trim();
            if (!screen.getName().equalsIgnoreCase(newName) && screenRepository.existsByTheaterIdAndNameIgnoreCase(screen.getTheater().getId(), newName)) {
                throw new IllegalArgumentException("Screen with name '" + newName + "' already exists in this theater");
            }
            screen.setName(newName);
        }
        if (request.getScreenType() != null) {
            screen.setScreenType(request.getScreenType());
        }
        if (request.getSoundSystem() != null) {
            screen.setSoundSystem(request.getSoundSystem().trim());
        }
        if (request.getActive() != null) {
            screen.setActive(request.getActive());
        }

        Screen updated = screenRepository.save(screen);
        log.info("Updated screen ID: {} ({})", updated.getId(), updated.getName());
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ScreenResponse getScreenById(UUID screenId) {
        Screen screen = screenRepository.findByIdWithTheater(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", screenId));
        return mapToResponse(screen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreenResponse> getScreensByTheater(UUID theaterId) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater", "id", theaterId);
        }
        return screenRepository.findByTheaterIdAndActiveTrue(theaterId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteScreen(UUID screenId, UserPrincipal currentUser) {
        Screen screen = screenRepository.findByIdWithTheater(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", screenId));

        validateOwnership(screen.getTheater(), currentUser);

        screen.setActive(false);
        screenRepository.save(screen);
        log.info("Soft-archived screen ID: {} ({})", screen.getId(), screen.getName());
    }

    private void validateOwnership(Theater theater, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !theater.getVendor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to manage screens for theater " + theater.getName());
        }
    }

    private ScreenResponse mapToResponse(Screen screen) {
        Theater theater = screen.getTheater();
        return new ScreenResponse(
                screen.getId(),
                (theater != null) ? theater.getId() : null,
                (theater != null) ? theater.getName() : null,
                screen.getName(),
                screen.getScreenType(),
                screen.getSoundSystem(),
                screen.getTotalSeats(),
                screen.isActive(),
                screen.getCreatedAt(),
                screen.getUpdatedAt()
        );
    }
}
