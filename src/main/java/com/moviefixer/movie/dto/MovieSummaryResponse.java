package com.moviefixer.movie.dto;

import com.moviefixer.movie.enums.ContentRating;
import com.moviefixer.movie.enums.MovieStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MovieSummaryResponse {

    private UUID id;
    private String title;
    private String slug;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private ContentRating contentRating;
    private MovieStatus status;
    private List<String> genreNames;
    private List<String> languageNames;

    public MovieSummaryResponse() {
    }

    public MovieSummaryResponse(
            UUID id,
            String title,
            String slug,
            Integer durationMinutes,
            LocalDate releaseDate,
            String posterUrl,
            ContentRating contentRating,
            MovieStatus status,
            List<String> genreNames,
            List<String> languageNames
    ) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
        this.contentRating = contentRating;
        this.status = status;
        this.genreNames = genreNames;
        this.languageNames = languageNames;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public ContentRating getContentRating() {
        return contentRating;
    }

    public void setContentRating(ContentRating contentRating) {
        this.contentRating = contentRating;
    }

    public MovieStatus getStatus() {
        return status;
    }

    public void setStatus(MovieStatus status) {
        this.status = status;
    }

    public List<String> getGenreNames() {
        return genreNames;
    }

    public List<String> getGenres() {
        return genreNames;
    }

    public void setGenreNames(List<String> genreNames) {
        this.genreNames = genreNames;
    }

    public List<String> getLanguageNames() {
        return languageNames;
    }

    public List<String> getLanguages() {
        return languageNames;
    }

    public void setLanguageNames(List<String> languageNames) {
        this.languageNames = languageNames;
    }
}

