package com.paris.chat_service.dto;

import com.paris.common.dto.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageStatusEventDTO {

    private String type;        // "STATUS"
    private Long messageId;
    private String senderId;
    private String receiverId;
    private MessageStatus status; // DELIVERED / SEEN
}
