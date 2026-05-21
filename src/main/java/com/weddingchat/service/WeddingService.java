package com.weddingchat.service;

import com.weddingchat.api.dto.JoinWeddingRequest;
import com.weddingchat.api.dto.JoinWeddingResponse;
import com.weddingchat.api.dto.MessageReactionResponse;
import com.weddingchat.api.dto.MessageResponse;
import com.weddingchat.domain.Guest;
import com.weddingchat.domain.Message;
import com.weddingchat.domain.MessageReaction;
import com.weddingchat.domain.Wedding;
import com.weddingchat.error.ApiException;
import com.weddingchat.repository.GuestRepository;
import com.weddingchat.repository.MessageReactionRepository;
import com.weddingchat.repository.MessageRepository;
import com.weddingchat.repository.WeddingRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeddingService {

    private final WeddingRepository weddingRepository;
    private final GuestRepository guestRepository;
    private final MessageRepository messageRepository;
    private final MessageReactionRepository reactionRepository;
    private final SessionTokenService sessionTokenService;
    private final GuestSessionService guestSessionService;

    public WeddingService(
            WeddingRepository weddingRepository,
            GuestRepository guestRepository,
            MessageRepository messageRepository,
            MessageReactionRepository reactionRepository,
            SessionTokenService sessionTokenService,
            GuestSessionService guestSessionService
    ) {
        this.weddingRepository = weddingRepository;
        this.guestRepository = guestRepository;
        this.messageRepository = messageRepository;
        this.reactionRepository = reactionRepository;
        this.sessionTokenService = sessionTokenService;
        this.guestSessionService = guestSessionService;
    }

    @Transactional
    public JoinWeddingResponse joinWedding(String slug, JoinWeddingRequest request) {
        Wedding wedding = weddingRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wedding not found."));
        if (!constantTimeEquals(wedding.getAccessCode(), request.accessCode().trim())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid wedding access code.");
        }

        Guest guest = new Guest();
        guest.setWedding(wedding);
        guest.setDisplayName(request.displayName().trim());
        guest.setSessionToken(sessionTokenService.generateToken());
        Guest savedGuest = guestRepository.save(guest);

        return new JoinWeddingResponse(
                wedding.getId(),
                savedGuest.getId(),
                savedGuest.getDisplayName(),
                savedGuest.getSessionToken()
        );
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(String slug, String sessionToken) {
        Wedding wedding = weddingRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wedding not found."));
        guestSessionService.requireGuestForWedding(wedding.getId(), sessionToken);
        return getMessagesForWedding(wedding.getId());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesForWedding(UUID weddingId) {
        List<Message> messages = messageRepository.findByWeddingIdOrderByCreatedAtAsc(weddingId);
        if (messages.isEmpty()) {
            return List.of();
        }

        List<UUID> messageIds = messages.stream().map(Message::getId).toList();
        List<MessageReaction> reactions = reactionRepository.findByMessageIdIn(messageIds);
        Map<UUID, List<MessageReactionResponse>> reactionsByMessageId = reactions.stream()
                .map(this::toReactionResponse)
                .collect(Collectors.groupingBy(MessageReactionResponse::messageId));

        return messages.stream()
                .map(message -> toMessageResponse(message, reactionsByMessageId.getOrDefault(message.getId(), Collections.emptyList())))
                .toList();
    }

    private MessageResponse toMessageResponse(Message message, List<MessageReactionResponse> reactions) {
        return new MessageResponse(
                message.getId(),
                message.getWedding().getId(),
                message.getGuest().getId(),
                message.getGuest().getDisplayName(),
                message.getContent(),
                message.getCreatedAt(),
                reactions
        );
    }

    public MessageReactionResponse toReactionResponse(MessageReaction reaction) {
        return new MessageReactionResponse(
                reaction.getId(),
                reaction.getMessage().getId(),
                reaction.getGuest().getId(),
                reaction.getGuest().getDisplayName(),
                reaction.getReaction(),
                reaction.getCreatedAt()
        );
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }
}
