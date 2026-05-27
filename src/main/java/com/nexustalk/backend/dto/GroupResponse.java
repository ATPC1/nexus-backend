package com.nexustalk.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class GroupResponse {
    private Long id;
    private String groupName;
    private String description;
    private String groupCode;
    private Long createdById;
    private LocalDateTime createdAt;
}
