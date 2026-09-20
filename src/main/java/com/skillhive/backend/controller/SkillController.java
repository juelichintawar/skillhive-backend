package com.skillhive.backend.controller;

import com.skillhive.backend.model.Skill;
import com.skillhive.backend.repository.SkillRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillRepository skillRepository;

    public SkillController(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @PostMapping
    public ResponseEntity<?> createSkill(@RequestBody Skill skill) {

        if (skillRepository.existsByName(skill.getName())) {
            return ResponseEntity.badRequest()
                    .body("Skill already exists");
        }

        Skill savedSkill = skillRepository.save(skill);

        return ResponseEntity.ok(savedSkill);
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {

        return ResponseEntity.ok(skillRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSkillById(@PathVariable Long id) {

        return skillRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }
}