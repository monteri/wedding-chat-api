package com.weddingchat.repository;

import com.weddingchat.domain.Guest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, UUID> {
    Optional<Guest> findBySessionToken(String sessionToken);
}
