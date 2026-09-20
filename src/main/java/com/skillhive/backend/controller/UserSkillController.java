package com.skillhive.backend.controller;

import com.skillhive.backend.model.Skill;
import com.skillhive.backend.model.User;
import com.skillhive.backend.model.UserSkill;
import com.skillhive.backend.repository.SkillRepository;
import com.skillhive.backend.repository.UserRepository;
import com.skillhive.backend.repository.UserSkillRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-skills")
public class UserSkillController {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public UserSkillController(
            UserSkillRepository userSkillRepository,
            UserRepository userRepository,
            SkillRepository skillRepository) {

        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    @PostMapping("/{skillId}")
    public ResponseEntity<?> addSkill(
            @PathVariable Long skillId,
            @RequestParam String proficiency,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setProficiency(proficiency);

        return ResponseEntity.ok(userSkillRepository.save(userSkill));
    }

    @GetMapping
    public ResponseEntity<List<UserSkill>> getMySkills(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                userSkillRepository.findByUserId(user.getId())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkill(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserSkill userSkill = userSkillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User skill not found"));

        if (!userSkill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You cannot delete this skill");
        }

        userSkillRepository.delete(userSkill);

        return ResponseEntity.ok("Skill removed successfully");
    }
}