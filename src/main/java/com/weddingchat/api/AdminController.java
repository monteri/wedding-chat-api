package com.weddingchat.api;

import com.weddingchat.api.dto.CreateWeddingRequest;
import com.weddingchat.api.dto.CreateWeddingResponse;
import com.weddingchat.api.dto.DeleteMessageResponse;
import com.weddingchat.api.dto.MessageResponse;
import com.weddingchat.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/weddings")
    public CreateWeddingResponse createWedding(
            @RequestHeader("X-Admin-Password") String adminPassword,
            @Valid @RequestBody CreateWeddingRequest request
    ) {
        return adminService.createWedding(request, adminPassword);
    }

    @GetMapping("/weddings/{slug}/messages")
    public List<MessageResponse> listMessages(
            @PathVariable String slug,
            @RequestHeader("X-Admin-Password") String adminPassword
    ) {
        return adminService.listMessages(slug, adminPassword);
    }

    @DeleteMapping("/messages/{messageId}")
    public DeleteMessageResponse deleteMessage(
            @PathVariable UUID messageId,
            @RequestHeader("X-Admin-Password") String adminPassword
    ) {
        return adminService.deleteMessage(messageId, adminPassword);
    }
}
