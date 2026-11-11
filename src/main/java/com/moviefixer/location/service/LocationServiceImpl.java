package com.moviefixer.location.service;

import com.moviefixer.location.dto.CitySummaryResponse;
import com.moviefixer.location.entity.City;
import com.moviefixer.location.entity.Country;
import com.moviefixer.location.repository.CityRepository;
import com.moviefixer.location.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    public LocationServiceImpl(CityRepository cityRepository, CountryRepository countryRepository) {
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    public List<CitySummaryResponse> getAllCities() {
        return cityRepository.findAllWithStateAndCountry().stream()
                .map(city -> new CitySummaryResponse(
                        city.getId(),
                        city.getName(),
                        city.getState() != null ? city.getState().getName() : null,
                        (city.getState() != null && city.getState().getCountry() != null) ? city.getState().getCountry().getName() : null,
                        (city.getState() != null && city.getState().getCountry() != null) ? city.getState().getCountry().getCode() : null
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    @Override
    public java.util.Optional<City> getCityById(java.util.UUID id) {
        return cityRepository.findById(id);
    }
}
