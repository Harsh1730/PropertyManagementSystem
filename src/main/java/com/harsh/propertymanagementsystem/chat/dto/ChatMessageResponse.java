package com.harsh.propertymanagementsystem.chat.dto;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long id;

    // Sender
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private Role senderRole;

    // Receiver
    private Long receiverId;
    private String receiverName;
    private String receiverEmail;
    private Role receiverRole;

    // Property Context
    private Long propertyId;
    private String propertyName;

    // Booking Context
    private Long bookingId;

    // Message
    private String message;
    private boolean isRead;
    private LocalDateTime timestamp;
}
