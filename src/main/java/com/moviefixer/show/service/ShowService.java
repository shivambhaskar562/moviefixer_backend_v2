package com.moviefixer.show.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.show.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShowService {

    ShowResponse createShow(CreateShowRequest request, UserPrincipal currentUser);

    BatchDailyScheduleResponse createBatchDailySchedule(BatchDailyScheduleRequest request, UserPrincipal currentUser);

    ShowResponse getShowById(UUID showId);

    List<MovieScheduleGroupResponse> getMovieScheduleByCityAndDate(UUID movieId, UUID cityId, LocalDate date);

    List<ShowSummaryResponse> getShowsByTheaterAndDate(UUID theaterId, LocalDate date);

    void cancelShow(UUID showId, UserPrincipal currentUser);
}
