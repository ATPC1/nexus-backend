package com.nexustalk.backend.controller;

import com.nexustalk.backend.dto.CreateGroupRequest;
import com.nexustalk.backend.dto.GroupResponse;
import com.nexustalk.backend.dto.JoinGroupRequest;
import com.nexustalk.backend.dto.MessageResponse;
import com.nexustalk.backend.entity.Group;
import com.nexustalk.backend.entity.GroupMember;
import com.nexustalk.backend.entity.User;
import com.nexustalk.backend.repository.GroupMemberRepository;
import com.nexustalk.backend.repository.GroupRepository;
import com.nexustalk.backend.repository.UserRepository;
import com.nexustalk.backend.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId()).orElseThrow();

        // Generate a 6-character random code
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Group group = Group.builder()
                .groupName(request.getGroupName())
                .description(request.getDescription())
                .groupCode(code)
                .createdBy(currentUser)
                .build();

        groupRepository.save(group);

        // Add creator as a member automatically
        GroupMember member = GroupMember.builder()
                .user(currentUser)
                .group(group)
                .build();
        
        groupMemberRepository.save(member);

        return ResponseEntity.ok(mapToResponse(group));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(@Valid @RequestBody JoinGroupRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId()).orElseThrow();

        Optional<Group> groupOpt = groupRepository.findByGroupCode(request.getGroupCode().toUpperCase());
        if (groupOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid group code."));
        }

        Group group = groupOpt.get();

        if (groupMemberRepository.existsByUserIdAndGroupId(currentUser.getId(), group.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You are already a member of this group."));
        }

        GroupMember member = GroupMember.builder()
                .user(currentUser)
                .group(group)
                .build();
        
        groupMemberRepository.save(member);

        return ResponseEntity.ok(mapToResponse(group));
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userDetails.getId());
        
        List<GroupResponse> groups = memberships.stream()
                .map(m -> mapToResponse(m.getGroup()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(groups);
    }

    private GroupResponse mapToResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .groupCode(group.getGroupCode())
                .createdById(group.getCreatedBy().getId())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
