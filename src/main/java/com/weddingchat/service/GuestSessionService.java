package com.weddingchat.service;

import com.weddingchat.domain.Guest;
import com.weddingchat.error.ApiException;
import com.weddingchat.repository.GuestRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GuestSessionService {

    private final GuestRepository guestRepository;

    public GuestSessionService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public Guest requireGuestForWedding(UUID weddingId, String sessionToken) {
        Guest guest = guestRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid guest session token."));
        if (!guest.getWedding().getId().equals(weddingId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Guest does not belong to this wedding.");
        }
        return guest;
    }
}
