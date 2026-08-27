package com.harsh.propertymanagementsystem.chat.mapper;

import com.harsh.propertymanagementsystem.chat.dto.ChatMessageResponse;
import com.harsh.propertymanagementsystem.chat.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatMessageResponse toResponse(ChatMessage entity) {
        if (entity == null) {
            return null;
        }

        return ChatMessageResponse.builder()
                .id(entity.getId())
                // Sender
                .senderId(entity.getSender() != null ? entity.getSender().getId() : null)
                .senderName(entity.getSender() != null ? entity.getSender().getName() : null)
                .senderEmail(entity.getSender() != null ? entity.getSender().getEmail() : null)
                .senderRole(entity.getSender() != null ? entity.getSender().getRole() : null)
                // Receiver
                .receiverId(entity.getReceiver() != null ? entity.getReceiver().getId() : null)
                .receiverName(entity.getReceiver() != null ? entity.getReceiver().getName() : null)
                .receiverEmail(entity.getReceiver() != null ? entity.getReceiver().getEmail() : null)
                .receiverRole(entity.getReceiver() != null ? entity.getReceiver().getRole() : null)
                // Property context
                .propertyId(entity.getProperty() != null ? entity.getProperty().getId() : null)
                .propertyName(entity.getProperty() != null ? entity.getProperty().getPropertyName() : null)
                // Booking context
                .bookingId(entity.getBooking() != null ? entity.getBooking().getId() : null)
                .message(entity.getMessage())
                .isRead(entity.isRead())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
