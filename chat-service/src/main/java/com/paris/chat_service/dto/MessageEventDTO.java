package com.paris.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageEventDTO {

    private String type;        // "MESSAGE"
    private String receiverId;
    private String content;
}
