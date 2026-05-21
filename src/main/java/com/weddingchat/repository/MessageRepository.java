package com.weddingchat.repository;

import com.weddingchat.domain.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByWeddingIdOrderByCreatedAtAsc(UUID weddingId);
}
