package com.nexustalk.backend.controller;

import com.nexustalk.backend.dto.MessageResponse;
import com.nexustalk.backend.dto.UserProfileResponse;
import com.nexustalk.backend.dto.UserUpdateRequest;
import com.nexustalk.backend.entity.User;
import com.nexustalk.backend.repository.UserRepository;
import com.nexustalk.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .dob(user.getDob())
                .bio(user.getBio())
                .profilePhoto(user.getProfilePhoto())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateRequest updateRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        if (updateRequest.getName() != null) user.setName(updateRequest.getName());
        if (updateRequest.getGender() != null) user.setGender(updateRequest.getGender());
        if (updateRequest.getDob() != null) user.setDob(updateRequest.getDob());
        if (updateRequest.getBio() != null) user.setBio(updateRequest.getBio());
        if (updateRequest.getProfilePhoto() != null) user.setProfilePhoto(updateRequest.getProfilePhoto());

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Profile updated successfully!"));
    }
}
