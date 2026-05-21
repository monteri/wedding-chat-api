package com.weddingchat.api;

import com.weddingchat.api.dto.JoinWeddingRequest;
import com.weddingchat.api.dto.JoinWeddingResponse;
import com.weddingchat.api.dto.MessageResponse;
import com.weddingchat.service.WeddingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weddings")
public class WeddingController {

    private final WeddingService weddingService;

    public WeddingController(WeddingService weddingService) {
        this.weddingService = weddingService;
    }

    @PostMapping("/{slug}/join")
    public JoinWeddingResponse joinWedding(
            @PathVariable String slug,
            @Valid @RequestBody JoinWeddingRequest request
    ) {
        return weddingService.joinWedding(slug, request);
    }

    @GetMapping("/{slug}/messages")
    public List<MessageResponse> getMessages(
            @PathVariable String slug,
            @RequestHeader("X-Guest-Token") String sessionToken
    ) {
        return weddingService.getMessages(slug, sessionToken);
    }
}
