package com.nexustalk.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserUpdateRequest {
    private String name;
    private String gender;
    private LocalDate dob;
    private String bio;
    private String profilePhoto;
}
