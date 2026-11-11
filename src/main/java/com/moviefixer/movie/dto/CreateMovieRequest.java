package com.moviefixer.movie.dto;

import com.moviefixer.movie.enums.ContentRating;
import com.moviefixer.movie.enums.MovieStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class CreateMovieRequest {

    @NotBlank(message = "Movie title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(max = 250, message = "Slug must not exceed 250 characters")
    private String slug;

    private String synopsis;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration cannot exceed 600 minutes")
    private Integer durationMinutes;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @Size(max = 500, message = "Poster URL must not exceed 500 characters")
    private String posterUrl;

    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    private String bannerUrl;

    @Size(max = 500, message = "Trailer URL must not exceed 500 characters")
    private String trailerUrl;

    @NotNull(message = "Content rating is required")
    private ContentRating contentRating;

    private MovieStatus status;

    @NotEmpty(message = "At least one genre must be associated with the movie")
    private Set<UUID> genreIds;

    @NotEmpty(message = "At least one language must be associated with the movie")
    private Set<UUID> languageIds;

    public CreateMovieRequest() {
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

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
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

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
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

    public Set<UUID> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(Set<UUID> genreIds) {
        this.genreIds = genreIds;
    }

    public Set<UUID> getLanguageIds() {
        return languageIds;
    }

    public void setLanguageIds(Set<UUID> languageIds) {
        this.languageIds = languageIds;
    }
}
