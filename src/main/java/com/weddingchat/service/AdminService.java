package com.weddingchat.service;

import com.weddingchat.api.dto.CreateWeddingRequest;
import com.weddingchat.api.dto.CreateWeddingResponse;
import com.weddingchat.api.dto.DeleteMessageResponse;
import com.weddingchat.api.dto.MessageResponse;
import com.weddingchat.config.AppProperties;
import com.weddingchat.domain.Message;
import com.weddingchat.domain.Wedding;
import com.weddingchat.error.ApiException;
import com.weddingchat.repository.MessageRepository;
import com.weddingchat.repository.WeddingRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final AppProperties appProperties;
    private final MessageRepository messageRepository;
    private final WeddingRepository weddingRepository;
    private final WeddingService weddingService;

    public AdminService(
            AppProperties appProperties,
            MessageRepository messageRepository,
            WeddingRepository weddingRepository,
            WeddingService weddingService
    ) {
        this.appProperties = appProperties;
        this.messageRepository = messageRepository;
        this.weddingRepository = weddingRepository;
        this.weddingService = weddingService;
    }

    @Transactional
    public CreateWeddingResponse createWedding(CreateWeddingRequest request, String adminPassword) {
        if (!matchesAdminPassword(adminPassword)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid admin password.");
        }

        String slug = request.slug().trim().toLowerCase();
        if (weddingRepository.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.CONFLICT, "A wedding with this slug already exists.");
        }

        Wedding wedding = new Wedding();
        wedding.setSlug(slug);
        wedding.setName(request.name().trim());
        wedding.setAccessCode(request.accessCode().trim());
        Wedding saved = weddingRepository.save(wedding);

        return new CreateWeddingResponse(
                saved.getId(),
                saved.getSlug(),
                saved.getName(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(String slug, String adminPassword) {
        if (!matchesAdminPassword(adminPassword)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid admin password.");
        }

        Wedding wedding = weddingRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wedding not found."));
        return weddingService.getMessagesForWedding(wedding.getId());
    }

    @Transactional
    public DeleteMessageResponse deleteMessage(UUID messageId, String adminPassword) {
        if (!matchesAdminPassword(adminPassword)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid admin password.");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message not found."));
        messageRepository.delete(message);
        return new DeleteMessageResponse(messageId, "deleted");
    }

    private boolean matchesAdminPassword(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                appProperties.getAdminPassword().getBytes(StandardCharsets.UTF_8),
                input.getBytes(StandardCharsets.UTF_8)
        );
    }
}
