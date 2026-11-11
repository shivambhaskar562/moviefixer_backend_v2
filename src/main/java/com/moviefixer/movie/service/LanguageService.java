package com.moviefixer.movie.service;

import com.moviefixer.movie.dto.LanguageRequest;
import com.moviefixer.movie.dto.LanguageResponse;

import java.util.List;
import java.util.UUID;

public interface LanguageService {

    List<LanguageResponse> getAllActiveLanguages();

    List<LanguageResponse> getAllLanguages();

    LanguageResponse getLanguageById(UUID id);

    LanguageResponse getLanguageByCode(String code);

    LanguageResponse createLanguage(LanguageRequest request);

    LanguageResponse updateLanguage(UUID id, LanguageRequest request);

    void deleteLanguage(UUID id);
}
