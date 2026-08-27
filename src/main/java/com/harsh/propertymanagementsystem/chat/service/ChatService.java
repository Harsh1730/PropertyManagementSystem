package com.harsh.propertymanagementsystem.chat.service;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.booking.entity.BookingRequest;
import com.harsh.propertymanagementsystem.booking.repository.BookingRequestRepository;
import com.harsh.propertymanagementsystem.chat.dto.ChatMessageResponse;
import com.harsh.propertymanagementsystem.chat.dto.ConversationSummaryResponse;
import com.harsh.propertymanagementsystem.chat.dto.SendChatMessageRequest;
import com.harsh.propertymanagementsystem.chat.entity.ChatMessage;
import com.harsh.propertymanagementsystem.chat.mapper.ChatMapper;
import com.harsh.propertymanagementsystem.chat.repository.ChatMessageRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRequestRepository bookingRepository;
    private final ChatMapper chatMapper;

    @Transactional
    public ChatMessageResponse sendMessage(SendChatMessageRequest request) {
        User sender = getCurrentUser();

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found with ID: " + request.getReceiverId()));

        Property property = null;
        if (request.getPropertyId() != null) {
            property = propertyRepository.findById(request.getPropertyId()).orElse(null);
        }

        BookingRequest booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId()).orElse(null);
            if (property == null && booking != null) {
                property = booking.getProperty();
            }
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .property(property)
                .booking(booking)
                .message(request.getMessage().trim())
                .isRead(false)
                .build();

        ChatMessage saved = chatRepository.save(message);
        log.info("Message sent from {} to {} for property {}", sender.getEmail(), receiver.getEmail(),
                property != null ? property.getId() : "N/A");

        return chatMapper.toResponse(saved);
    }

    @Transactional
    public List<ChatMessageResponse> getConversation(Long otherUserId) {
        User currentUser = getCurrentUser();

        // Mark incoming messages as read
        chatRepository.markMessagesAsRead(otherUserId, currentUser.getId());

        return chatRepository.findConversationBetweenUsers(currentUser.getId(), otherUserId)
                .stream()
                .map(chatMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getBookingMessages(Long bookingId) {
        return chatRepository.findByBookingIdOrderByTimestampAsc(bookingId)
                .stream()
                .map(chatMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> getConversationsInbox() {
        User currentUser = getCurrentUser();
        List<ChatMessage> allMessages = chatRepository.findAllMessagesForUser(currentUser.getId());

        // Group by other user ID to pick latest message & property context
        Map<Long, ChatMessage> latestByOtherUser = new LinkedHashMap<>();
        for (ChatMessage msg : allMessages) {
            User other = msg.getSender().getId().equals(currentUser.getId()) ? msg.getReceiver() : msg.getSender();
            if (!latestByOtherUser.containsKey(other.getId())) {
                latestByOtherUser.put(other.getId(), msg);
            }
        }

        List<ConversationSummaryResponse> summaries = new ArrayList<>();
        for (Map.Entry<Long, ChatMessage> entry : latestByOtherUser.entrySet()) {
            Long otherId = entry.getKey();
            ChatMessage lastMsg = entry.getValue();
            User otherUser = lastMsg.getSender().getId().equals(currentUser.getId())
                    ? lastMsg.getReceiver()
                    : lastMsg.getSender();

            long unread = chatRepository.countBySenderIdAndReceiverIdAndIsReadFalse(otherId, currentUser.getId());

            Property prop = lastMsg.getProperty();
            if (prop == null && lastMsg.getBooking() != null) {
                prop = lastMsg.getBooking().getProperty();
            }

            summaries.add(ConversationSummaryResponse.builder()
                    .otherUserId(otherUser.getId())
                    .otherUserName(otherUser.getName())
                    .otherUserEmail(otherUser.getEmail())
                    .otherUserRole(otherUser.getRole())
                    .otherUserPhoneNumber(otherUser.getPhoneNumber())
                    .propertyId(prop != null ? prop.getId() : null)
                    .propertyName(prop != null ? prop.getPropertyName() : null)
                    .lastMessage(lastMsg.getMessage())
                    .lastMessageTimestamp(lastMsg.getTimestamp())
                    .unreadCount(unread)
                    .build());
        }

        return summaries;
    }

    @Transactional
    public void markAsRead(Long otherUserId) {
        User currentUser = getCurrentUser();
        chatRepository.markMessagesAsRead(otherUserId, currentUser.getId());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
