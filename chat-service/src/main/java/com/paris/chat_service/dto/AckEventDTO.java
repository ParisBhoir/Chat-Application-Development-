package com.paris.chat_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AckEventDTO {
    private String type;      // "ACK"
    private Long messageId;
    private String senderId;
    private String receiverId;
}
