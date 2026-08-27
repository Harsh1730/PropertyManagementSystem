package com.harsh.propertymanagementsystem.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendChatMessageRequest {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    private Long propertyId;

    private Long bookingId;

    @NotBlank(message = "Message content cannot be blank")
    private String message;
}
