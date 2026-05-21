package com.weddingchat.repository;

import com.weddingchat.domain.MessageReaction;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, UUID> {
    List<MessageReaction> findByMessageIdIn(Collection<UUID> messageIds);
}
