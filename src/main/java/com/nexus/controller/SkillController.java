package com.nexus.controller;

import com.nexus.dto.AddSkillRequest;
import com.nexus.dto.SkillDto;
import com.nexus.entity.User;
import com.nexus.service.SkillService;
import com.nexus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;
    private final UserService userService;

    public SkillController(SkillService skillService, UserService userService) {
        this.skillService = skillService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<SkillDto>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @PostMapping
    public ResponseEntity<?> addSkillToProfile(@Valid @RequestBody AddSkillRequest request,
                                               Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        skillService.addSkillToUser(user.getId(), request);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Skill successfully added to profile");
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{userSkillId}")
    public ResponseEntity<?> removeSkillFromProfile(@PathVariable Long userSkillId,
                                                    Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        skillService.removeUserSkill(user.getId(), userSkillId);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Skill removed");
        return ResponseEntity.ok(res);
    }
}
