package com.harsh.propertymanagementsystem.chat.dto;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryResponse {

    private Long otherUserId;
    private String otherUserName;
    private String otherUserEmail;
    private Role otherUserRole;
    private String otherUserPhoneNumber;

    private Long propertyId;
    private String propertyName;

    private String lastMessage;
    private LocalDateTime lastMessageTimestamp;
    private long unreadCount;
}
