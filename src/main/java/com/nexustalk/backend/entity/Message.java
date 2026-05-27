package com.nexustalk.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_ai", nullable = false)
    private boolean isAi;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "reply_to_id")
    private Long replyToId;

    @Column(name = "reply_to_sender_name")
    private String replyToSenderName;

    @Column(name = "reply_to_content", columnDefinition = "TEXT")
    private String replyToContent;
}
