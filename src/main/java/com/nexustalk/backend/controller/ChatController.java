package com.nexustalk.backend.controller;

import com.nexustalk.backend.dto.ChatMessageDTO;
import com.nexustalk.backend.entity.Group;
import com.nexustalk.backend.entity.Message;
import com.nexustalk.backend.entity.User;
import com.nexustalk.backend.repository.GroupRepository;
import com.nexustalk.backend.repository.MessageRepository;
import com.nexustalk.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import com.nexustalk.backend.security.UserDetailsImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private com.nexustalk.backend.service.GeminiService geminiService;

    @MessageMapping("/chat/{groupId}/sendMessage")
    @org.springframework.transaction.annotation.Transactional
    public void sendMessage(@DestinationVariable Long groupId, @Payload ChatMessageDTO chatMessageDTO) {
        User sender = userRepository.findById(chatMessageDTO.getSenderId()).orElseThrow();
        Group group = groupRepository.findById(groupId).orElseThrow();

        Message message = Message.builder()
                .sender(sender)
                .group(group)
                .message(chatMessageDTO.getContent())
                .isAi(false)
                .replyToId(chatMessageDTO.getReplyToId())
                .replyToSenderName(chatMessageDTO.getReplyToSenderName())
                .replyToContent(chatMessageDTO.getReplyToContent())
                .build();

        Message savedMessage = messageRepository.save(message);

        ChatMessageDTO responseDto = ChatMessageDTO.builder()
                .id(savedMessage.getId())
                .groupId(groupId)
                .senderId(sender.getId())
                .senderName(sender.getName())
                .senderPhoto(sender.getProfilePhoto())
                .content(savedMessage.getMessage())
                .isAi(false)
                .timestamp(savedMessage.getTimestamp())
                .replyToId(savedMessage.getReplyToId())
                .replyToSenderName(savedMessage.getReplyToSenderName())
                .replyToContent(savedMessage.getReplyToContent())
                .build();

        messagingTemplate.convertAndSend("/topic/group/" + groupId, responseDto);
        
        // AI Logic Check
        String msgContent = chatMessageDTO.getContent().toLowerCase();
        if (msgContent.contains("@tom") || msgContent.startsWith("hey tom") || msgContent.startsWith("hi tom") || msgContent.startsWith("hello tom") || msgContent.startsWith("tom,") || msgContent.startsWith("tom ")) {
            // Fetch context (last 5 messages)
            List<Message> recent = messageRepository.findByGroupIdOrderByTimestampAsc(groupId);
            String context = recent.stream()
                .skip(Math.max(0, recent.size() - 5))
                .map(m -> (m.isAi() ? "TOM" : m.getSender().getName()) + ": " + m.getMessage())
                .collect(Collectors.joining("\n"));

            String aiResponseContent = geminiService.getAiResponse(chatMessageDTO.getContent(), context);

            Message aiMessage = Message.builder()
                    .sender(null) // AI has no sender entity
                    .group(group)
                    .message(aiResponseContent)
                    .isAi(true)
                    .build();

            Message savedAiMessage = messageRepository.save(aiMessage);

            ChatMessageDTO aiResponseDto = ChatMessageDTO.builder()
                    .id(savedAiMessage.getId())
                    .groupId(groupId)
                    .senderId(null)
                    .senderName("TOM")
                    .senderPhoto(null)
                    .content(savedAiMessage.getMessage())
                    .isAi(true)
                    .timestamp(savedAiMessage.getTimestamp())
                    .build();

            messagingTemplate.convertAndSend("/topic/group/" + groupId, aiResponseDto);
        }
    }

    @GetMapping("/api/messages/{groupId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Long groupId) {
        List<Message> messages = messageRepository.findByGroupIdOrderByTimestampAsc(groupId);
        
        List<ChatMessageDTO> dtos = messages.stream().map(m -> ChatMessageDTO.builder()
                .id(m.getId())
                .groupId(m.getGroup().getId())
                .senderId(m.getSender() != null ? m.getSender().getId() : null)
                .senderName(m.getSender() != null ? m.getSender().getName() : "AI")
                .senderPhoto(m.getSender() != null ? m.getSender().getProfilePhoto() : null)
                .content(m.getMessage())
                .isAi(m.isAi())
                .timestamp(m.getTimestamp())
                .replyToId(m.getReplyToId())
                .replyToSenderName(m.getReplyToSenderName())
                .replyToContent(m.getReplyToContent())
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/api/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Message message = messageRepository.findById(messageId).orElse(null);
        
        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        if (message.getSender() != null && message.getSender().getId().equals(userDetails.getId())) {
            messageRepository.delete(message);
            // Broadcast delete event
            messagingTemplate.convertAndSend("/topic/group/" + message.getGroup().getId() + "/delete", messageId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).body("Not authorized to delete this message");
        }
    }

    @DeleteMapping("/api/messages/group/{groupId}")
    public ResponseEntity<?> clearGroupChat(@PathVariable Long groupId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Group group = groupRepository.findById(groupId).orElse(null);

        if (group == null) {
            return ResponseEntity.notFound().build();
        }

        if (group.getCreatedBy().getId().equals(userDetails.getId())) {
            messageRepository.deleteByGroupId(groupId);
            // Broadcast clear event
            messagingTemplate.convertAndSend("/topic/group/" + groupId + "/clear", "clear");
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).body("Only the group creator can clear the chat");
        }
    }
}
