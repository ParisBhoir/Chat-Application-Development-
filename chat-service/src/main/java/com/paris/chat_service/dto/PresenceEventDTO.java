package com.paris.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresenceEventDTO {

    private String type;     // PRESENCE
    private PresenceStatus status;   // ONLINE / OFFLINE
    private String userId;

}

