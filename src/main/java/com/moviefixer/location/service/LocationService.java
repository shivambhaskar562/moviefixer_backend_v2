package com.moviefixer.location.service;

import com.moviefixer.location.dto.CitySummaryResponse;
import com.moviefixer.location.entity.City;
import com.moviefixer.location.entity.Country;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface LocationService {

    List<CitySummaryResponse> getAllCities();

    List<Country> getAllCountries();

    Optional<City> getCityById(UUID id);
}
