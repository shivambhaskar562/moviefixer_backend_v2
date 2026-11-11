package com.moviefixer.booking.controller;

import com.moviefixer.auth.security.CurrentUser;
import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.booking.dto.TicketResponse;

import com.moviefixer.booking.dto.ValidateQrRequest;
import com.moviefixer.booking.dto.ValidateQrResponse;
import com.moviefixer.booking.service.TicketValidationService;
import com.moviefixer.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets & Gate Admission", description = "Endpoints for ticket verification, gate staff QR code scanning, and admission management")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketValidationService ticketValidationService;

    public TicketController(TicketValidationService ticketValidationService) {
        this.ticketValidationService = ticketValidationService;
    }

    @PostMapping("/validate-qr")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_MANAGER', 'THEATER_TICKET_STAFF', 'THEATER_VENDOR')")
    @Operation(summary = "Validate digital QR pass at gate", description = "Decodes cryptographic QR payload, verifies HMAC signature, records admission, and alerts on duplicate scans")
    public ResponseEntity<ApiResponse<ValidateQrResponse>> validateQr(
            @Valid @RequestBody ValidateQrRequest request,
            @CurrentUser UserPrincipal gateStaff) {

        ValidateQrResponse response = ticketValidationService.validateAndAdmit(request, gateStaff);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    @GetMapping("/{ticketNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get ticket details by ticket number", description = "Retrieves ticket details including seat information and check-in status")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(
            @PathVariable("ticketNumber") String ticketNumber,
            @CurrentUser UserPrincipal currentUser) {

        TicketResponse response = ticketValidationService.getTicketByNumber(ticketNumber, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ticket details retrieved successfully", response));
    }
}
