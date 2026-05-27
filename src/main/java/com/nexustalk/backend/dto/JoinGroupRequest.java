package com.nexustalk.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinGroupRequest {
    @NotBlank
    private String groupCode;
}
