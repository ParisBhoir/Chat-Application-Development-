package com.paris.chat_service.controller;

import com.paris.chat_service.model.Message;
import com.paris.chat_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        return ResponseEntity.ok(chatService.sendMessage(message));
    }

    @GetMapping("/history/{senderId}/{receiverId}")
    public ResponseEntity<List<Message>> getChatHistory(
            @PathVariable String senderId,
            @PathVariable String receiverId) {
        return ResponseEntity.ok(chatService.getChatHistory(senderId, receiverId));
    }

}
