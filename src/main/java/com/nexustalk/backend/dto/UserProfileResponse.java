package com.nexustalk.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String gender;
    private LocalDate dob;
    private String profilePhoto;
    private String bio;
    private LocalDateTime createdAt;
}
