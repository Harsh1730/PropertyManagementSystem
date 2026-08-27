package com.harsh.propertymanagementsystem.chat.controller;

import com.harsh.propertymanagementsystem.chat.dto.ChatMessageResponse;
import com.harsh.propertymanagementsystem.chat.dto.ConversationSummaryResponse;
import com.harsh.propertymanagementsystem.chat.dto.SendChatMessageRequest;
import com.harsh.propertymanagementsystem.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Valid @RequestBody SendChatMessageRequest request) {
        log.info("Received request to send message to user {}", request.getReceiverId());
        return ResponseEntity.ok(chatService.sendMessage(request));
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(
            @PathVariable Long otherUserId) {
        log.info("Received request to get conversation with user {}", otherUserId);
        return ResponseEntity.ok(chatService.getConversation(otherUserId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<ChatMessageResponse>> getBookingMessages(
            @PathVariable Long bookingId) {
        log.info("Received request to get messages for booking {}", bookingId);
        return ResponseEntity.ok(chatService.getBookingMessages(bookingId));
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<ConversationSummaryResponse>> getInbox() {
        log.info("Received request to get chat inbox threads");
        return ResponseEntity.ok(chatService.getConversationsInbox());
    }

    @PatchMapping("/read/{otherUserId}")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long otherUserId) {
        log.info("Received request to mark messages as read for user {}", otherUserId);
        chatService.markAsRead(otherUserId);
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }
}
