package com.weddingchat.service;

import com.weddingchat.api.dto.MessageReactionResponse;
import com.weddingchat.api.dto.MessageResponse;
import com.weddingchat.config.AppProperties;
import com.weddingchat.domain.Guest;
import com.weddingchat.domain.Message;
import com.weddingchat.domain.MessageReaction;
import com.weddingchat.domain.Wedding;
import com.weddingchat.error.ApiException;
import com.weddingchat.repository.MessageReactionRepository;
import com.weddingchat.repository.MessageRepository;
import com.weddingchat.repository.WeddingRepository;
import com.weddingchat.websocket.dto.ChatReactRequest;
import com.weddingchat.websocket.dto.ChatSendRequest;
import com.weddingchat.websocket.dto.FloatingReactionEvent;
import com.weddingchat.websocket.dto.FloatingReactionRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final WeddingRepository weddingRepository;
    private final GuestSessionService guestSessionService;
    private final MessageRepository messageRepository;
    private final MessageReactionRepository reactionRepository;
    private final WeddingService weddingService;
    private final RateLimitService rateLimitService;
    private final AppProperties appProperties;

    public ChatService(
            WeddingRepository weddingRepository,
            GuestSessionService guestSessionService,
            MessageRepository messageRepository,
            MessageReactionRepository reactionRepository,
            WeddingService weddingService,
            RateLimitService rateLimitService,
            AppProperties appProperties
    ) {
        this.weddingRepository = weddingRepository;
        this.guestSessionService = guestSessionService;
        this.messageRepository = messageRepository;
        this.reactionRepository = reactionRepository;
        this.weddingService = weddingService;
        this.rateLimitService = rateLimitService;
        this.appProperties = appProperties;
    }

    @Transactional
    public MessageResponse sendMessage(ChatSendRequest request) {
        Wedding wedding = weddingRepository.findById(request.weddingId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wedding not found."));
        Guest guest = guestSessionService.requireGuestForWedding(wedding.getId(), request.sessionToken());

        rateLimitService.checkOrThrow(
                "msg:" + guest.getSessionToken(),
                appProperties.getRateLimit().getMessagePerMinute(),
                "Message rate limit exceeded. Try again shortly."
        );

        Message message = new Message();
        message.setWedding(wedding);
        message.setGuest(guest);
        message.setContent(request.content().trim());
        Message saved = messageRepository.save(message);

        return new MessageResponse(
                saved.getId(),
                wedding.getId(),
                guest.getId(),
                guest.getDisplayName(),
                saved.getContent(),
                saved.getCreatedAt(),
                List.of()
        );
    }

    @Transactional
    public MessageReactionResponse reactToMessage(ChatReactRequest request) {
        Guest guest = guestSessionService.requireGuestForWedding(request.weddingId(), request.sessionToken());
        Message message = messageRepository.findById(request.messageId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message not found."));

        if (!message.getWedding().getId().equals(request.weddingId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message does not belong to the provided wedding.");
        }

        MessageReaction reaction = new MessageReaction();
        reaction.setMessage(message);
        reaction.setGuest(guest);
        reaction.setReaction(request.reaction().trim());
        MessageReaction saved = reactionRepository.save(reaction);
        return weddingService.toReactionResponse(saved);
    }

    @Transactional(readOnly = true)
    public FloatingReactionEvent floatingReaction(FloatingReactionRequest request) {
        Guest guest = guestSessionService.requireGuestForWedding(request.weddingId(), request.sessionToken());
        rateLimitService.checkOrThrow(
                "float:" + guest.getSessionToken(),
                appProperties.getRateLimit().getFloatingPerMinute(),
                "Floating reaction rate limit exceeded. Try again shortly."
        );
        return new FloatingReactionEvent(
                request.weddingId(),
                guest.getId(),
                guest.getDisplayName(),
                request.reaction().trim(),
                OffsetDateTime.now()
        );
    }
}
