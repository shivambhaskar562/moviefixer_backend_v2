package com.moviefixer.movie.service;

import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.movie.dto.LanguageRequest;
import com.moviefixer.movie.dto.LanguageResponse;
import com.moviefixer.movie.entity.Language;
import com.moviefixer.movie.repository.LanguageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class LanguageServiceImpl implements LanguageService {

    private static final Logger log = LoggerFactory.getLogger(LanguageServiceImpl.class);

    private final LanguageRepository languageRepository;

    public LanguageServiceImpl(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageResponse> getAllActiveLanguages() {
        return languageRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageResponse> getAllLanguages() {
        return languageRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageResponse getLanguageById(UUID id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "id", id));
        return mapToResponse(language);
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageResponse getLanguageByCode(String code) {
        Language language = languageRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "code", code));
        return mapToResponse(language);
    }

    @Override
    @Transactional
    public LanguageResponse createLanguage(LanguageRequest request) {
        String code = request.getCode().trim().toLowerCase(Locale.ROOT);

        if (languageRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Language with code '" + code + "' already exists");
        }

        Language language = new Language(
                request.getName().trim(),
                code
        );
        language.setActive(request.isActive());

        Language saved = languageRepository.save(language);
        log.info("Created new movie language: {} [{}]", saved.getName(), saved.getCode());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public LanguageResponse updateLanguage(UUID id, LanguageRequest request) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "id", id));

        if (request.getName() != null && !request.getName().isBlank()) {
            language.setName(request.getName().trim());
        }
        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim().toLowerCase(Locale.ROOT);
            if (!language.getCode().equalsIgnoreCase(newCode) && languageRepository.existsByCodeIgnoreCase(newCode)) {
                throw new IllegalArgumentException("Language with code '" + newCode + "' already exists");
            }
            language.setCode(newCode);
        }
        language.setActive(request.isActive());

        Language updated = languageRepository.save(language);
        log.info("Updated language: {}", updated.getCode());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteLanguage(UUID id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "id", id));
        language.setActive(false);
        languageRepository.save(language);
        log.info("Deactivated language ID: {}", id);
    }

    private LanguageResponse mapToResponse(Language language) {
        return new LanguageResponse(
                language.getId(),
                language.getName(),
                language.getCode(),
                language.isActive()
        );
    }
}
