package com.nexustalk.backend.controller;

import com.nexustalk.backend.dto.JwtResponse;
import com.nexustalk.backend.dto.LoginRequest;
import com.nexustalk.backend.dto.MessageResponse;
import com.nexustalk.backend.dto.SignupRequest;
import com.nexustalk.backend.entity.User;
import com.nexustalk.backend.repository.UserRepository;
import com.nexustalk.backend.security.JwtUtils;
import com.nexustalk.backend.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getName(),
                    userDetails.getEmail()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(401).body(new MessageResponse("Error: Invalid email or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .userId(signUpRequest.getUserId())
                .password(encoder.encode(signUpRequest.getPassword()))
                .gender(signUpRequest.getGender())
                .dob(signUpRequest.getDob())
                .profilePhoto(signUpRequest.getProfilePhoto())
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Autowired
    com.nexustalk.backend.service.CloudinaryService cloudinaryService;

    @PostMapping("/upload-profile-pic")
    public ResponseEntity<?> uploadProfilePic(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String fileUrl = cloudinaryService.uploadFile(file);
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("fileUrl", fileUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error uploading file: " + e.getMessage()));
        }
    }
}
