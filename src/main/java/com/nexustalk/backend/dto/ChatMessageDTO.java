package com.nexustalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String senderName;
    private String senderPhoto;
    private String content;
    private boolean isAi;
    private LocalDateTime timestamp;
    private Long replyToId;
    private String replyToSenderName;
    private String replyToContent;
}
