package com.nexustalk.backend.controller;

import com.nexustalk.backend.entity.FileEntity;
import com.nexustalk.backend.entity.Group;
import com.nexustalk.backend.entity.User;
import com.nexustalk.backend.repository.FileRepository;
import com.nexustalk.backend.repository.GroupRepository;
import com.nexustalk.backend.repository.UserRepository;
import com.nexustalk.backend.security.UserDetailsImpl;
import com.nexustalk.backend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @PostMapping("/upload/{groupId}")
    public ResponseEntity<?> uploadFile(@PathVariable Long groupId, @RequestParam("file") MultipartFile file) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = userRepository.findById(userDetails.getId()).orElseThrow();
            Group group = groupRepository.findById(groupId).orElseThrow();

            // Upload to Cloudinary
            String fileUrl = cloudinaryService.uploadFile(file);

            FileEntity fileEntity = FileEntity.builder()
                    .fileUrl(fileUrl)
                    .fileType(file.getContentType())
                    .uploadedBy(currentUser)
                    .group(group)
                    .build();

            fileRepository.save(fileEntity);

            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("fileType", file.getContentType());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Could not upload the file: " + e.getMessage());
        }
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<FileEntity>> getGroupFiles(@PathVariable Long groupId) {
        List<FileEntity> files = fileRepository.findByGroupIdOrderByUploadedAtDesc(groupId);
        return ResponseEntity.ok(files);
    }
}
