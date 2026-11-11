package com.moviefixer.booking.controller;

import com.moviefixer.auth.security.CurrentUser;
import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.booking.dto.*;
import com.moviefixer.booking.service.BookingService;
import com.moviefixer.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Engine", description = "Endpoints for initiating, confirming, and managing cinema ticket bookings with zero DB row locking")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initiate checkout booking session", description = "Reserves selected seats for 10 minutes and generates booking reference")
    public ResponseEntity<ApiResponse<InitiateBookingResponse>> initiateBooking(
            @Valid @RequestBody InitiateBookingRequest request,
            @CurrentUser UserPrincipal currentUser) {

        InitiateBookingResponse response = bookingService.initiateBooking(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Booking session initiated successfully", response));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirm booking & issue QR passes", description = "Finalizes booking with atomic query versioning CAS and generates cryptographically signed QR tickets")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable("id") UUID bookingId,
            @CurrentUser UserPrincipal currentUser) {

        BookingResponse response = bookingService.confirmBooking(bookingId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed and digital QR tickets issued", response));
    }

    @PostMapping("/direct")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Direct one-step booking", description = "Locks and confirms seats atomically using query versioning in a single request")
    public ResponseEntity<ApiResponse<BookingResponse>> directBooking(
            @Valid @RequestBody DirectBookingRequest request,
            @CurrentUser UserPrincipal currentUser) {

        BookingResponse response = bookingService.directBooking(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Direct booking confirmed and digital passes issued", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get booking details by ID", description = "Fetches complete booking details with ticket items and digital QR codes")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable("id") UUID bookingId,
            @CurrentUser UserPrincipal currentUser) {

        BookingResponse response = bookingService.getBookingById(bookingId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Booking details retrieved successfully", response));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current customer's booking history", description = "Returns paginated list of bookings created by the authenticated customer")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BookingResponse> bookings = bookingService.getMyBookings(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("Customer bookings retrieved successfully", bookings));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel confirmed booking", description = "Cancels booking, releases seats to inventory, and calculates policy refund amount")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBooking(
            @PathVariable("id") UUID bookingId,
            @CurrentUser UserPrincipal currentUser) {

        CancelBookingResponse response = bookingService.cancelBooking(bookingId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }
}
