package com.moviefixer.analytics.service;

import com.moviefixer.analytics.dto.*;
import com.moviefixer.auth.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface AnalyticsService {

    PlatformOverviewResponse getPlatformOverview(UserPrincipal currentUser);

    List<TopMovieAnalyticsResponse> getTopMovies(int limit, UserPrincipal currentUser);

    List<CityAnalyticsResponse> getCityAnalytics(UserPrincipal currentUser);

    List<DailyRevenueTrendResponse> getDailyRevenueTrend(int days, UserPrincipal currentUser);

    VendorOverviewResponse getVendorOverview(UserPrincipal currentUser);

    List<VendorTheaterAnalyticsResponse> getVendorTheaters(UserPrincipal currentUser);

    List<VendorMovieAnalyticsResponse> getVendorMovies(UserPrincipal currentUser);

    ShowOccupancyAnalyticsResponse getShowOccupancy(UUID showId, UserPrincipal currentUser);
}
