package com.weddingchat.websocket;

import com.weddingchat.api.dto.MessageReactionResponse;
import com.weddingchat.api.dto.MessageResponse;
import com.weddingchat.service.ChatService;
import com.weddingchat.websocket.dto.ChatReactRequest;
import com.weddingchat.websocket.dto.ChatSendRequest;
import com.weddingchat.websocket.dto.FloatingReactionEvent;
import com.weddingchat.websocket.dto.FloatingReactionRequest;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid ChatSendRequest request) {
        MessageResponse message = chatService.sendMessage(request);
        String topic = "/topic/weddings/" + message.weddingId() + "/messages";
        messagingTemplate.convertAndSend(topic, message);
    }

    @MessageMapping("/chat.react")
    public void reactToMessage(@Valid ChatReactRequest request) {
        MessageReactionResponse reaction = chatService.reactToMessage(request);
        String topic = "/topic/weddings/" + request.weddingId() + "/message-reactions";
        messagingTemplate.convertAndSend(topic, reaction);
    }

    @MessageMapping("/reactions.float")
    public void floatingReaction(@Valid FloatingReactionRequest request) {
        FloatingReactionEvent event = chatService.floatingReaction(request);
        String topic = "/topic/weddings/" + request.weddingId() + "/floating-reactions";
        messagingTemplate.convertAndSend(topic, event);
    }
}
