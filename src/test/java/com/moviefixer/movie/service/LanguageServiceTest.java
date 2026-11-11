package com.moviefixer.movie.service;

import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.movie.dto.LanguageRequest;
import com.moviefixer.movie.dto.LanguageResponse;
import com.moviefixer.movie.entity.Language;
import com.moviefixer.movie.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LanguageService Unit Tests")
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageServiceImpl languageService;

    private Language testLanguage;
    private UUID languageId;

    @BeforeEach
    void setUp() {
        languageId = UUID.randomUUID();
        testLanguage = new Language(languageId, "English", "en", true);
    }

    @Test
    @DisplayName("createLanguage successfully creates new Language")
    void createLanguage_success() {
        LanguageRequest request = new LanguageRequest("English", "en", true);

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);
        when(languageRepository.save(any(Language.class))).thenReturn(testLanguage);

        LanguageResponse response = languageService.createLanguage(request);

        assertNotNull(response);
        assertEquals("English", response.getName());
        assertEquals("en", response.getCode());
        verify(languageRepository, times(1)).save(any(Language.class));
    }

    @Test
    @DisplayName("createLanguage throws IllegalArgumentException when duplicate code exists")
    void createLanguage_duplicateCode_throwsException() {
        LanguageRequest request = new LanguageRequest("English", "en", true);
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> languageService.createLanguage(request));
        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    @DisplayName("getLanguageById returns LanguageResponse when found")
    void getLanguageById_found() {
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(testLanguage));

        LanguageResponse response = languageService.getLanguageById(languageId);

        assertNotNull(response);
        assertEquals(languageId, response.getId());
        assertEquals("English", response.getName());
    }

    @Test
    @DisplayName("getLanguageById throws ResourceNotFoundException when not found")
    void getLanguageById_notFound_throwsException() {
        UUID missingId = UUID.randomUUID();
        when(languageRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> languageService.getLanguageById(missingId));
    }

    @Test
    @DisplayName("getAllActiveLanguages returns list of active languages")
    void getAllActiveLanguages_returnsList() {
        when(languageRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(testLanguage));

        List<LanguageResponse> result = languageService.getAllActiveLanguages();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("en", result.get(0).getCode());
    }
}
