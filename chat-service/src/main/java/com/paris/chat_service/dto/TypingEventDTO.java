package com.paris.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingEventDTO {

    private String type;      // "TYPING"
    private String senderId;
    private String receiverId;
    private boolean typing;   // true / false
}
