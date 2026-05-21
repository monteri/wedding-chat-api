package com.weddingchat.repository;

import com.weddingchat.domain.Wedding;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeddingRepository extends JpaRepository<Wedding, UUID> {
    Optional<Wedding> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
