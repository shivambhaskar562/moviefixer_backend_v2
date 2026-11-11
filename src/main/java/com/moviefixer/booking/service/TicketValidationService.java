package com.moviefixer.booking.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.booking.dto.TicketResponse;
import com.moviefixer.booking.dto.ValidateQrRequest;
import com.moviefixer.booking.dto.ValidateQrResponse;

public interface TicketValidationService {

    /**
     * Scans and validates a digital QR pass for theater admission.
     * Enforces tamper-proof cryptographic signature check and duplicate scan prevention.
     *
     * @param request the QR validation request containing the scanned QR payload
     * @param gateStaff the authenticated theater gate staff member
     * @return ValidateQrResponse indicating admission status and ticket metadata
     */
    ValidateQrResponse validateAndAdmit(ValidateQrRequest request, UserPrincipal gateStaff);

    /**
     * Look up ticket details by unique ticket number.
     */
    TicketResponse getTicketByNumber(String ticketNumber, UserPrincipal currentUser);
}
