package com.nexus;

import com.nexus.dto.CompatibilityResponse;
import com.nexus.entity.Skill;
import com.nexus.entity.User;
import com.nexus.entity.UserSkill;
import com.nexus.repository.UserSkillRepository;
import com.nexus.service.CompatibilityService;
import com.nexus.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CompatibilityServiceTest {

    private UserSkillRepository userSkillRepository;
    private UserService userService;
    private CompatibilityService compatibilityService;

    @BeforeEach
    void setUp() {
        userSkillRepository = Mockito.mock(UserSkillRepository.class);
        userService = Mockito.mock(UserService.class);
        compatibilityService = new CompatibilityService(userSkillRepository, userService);
    }

    @Test
    void testComplementarySkillCompatibility() {
        User u1 = new User("Priya Kulkarni", "priya@nitt.edu", "pass", "NIT Trichy", "ECE", "Year 3", "Frontend dev");
        u1.setId(1L);
        User u2 = new User("Arjun Mehta", "arjun@iitd.ac.in", "pass", "IIT Delhi", "CS", "Year 3", "Backend dev");
        u2.setId(2L);

        when(userService.findById(1L)).thenReturn(u1);
        when(userService.findById(2L)).thenReturn(u2);

        Skill react = new Skill("React", "Frontend");
        Skill springBoot = new Skill("Spring Boot", "Backend");

        UserSkill s1 = new UserSkill(u1, react, "PROFICIENT", "ADVANCED");
        UserSkill s2 = new UserSkill(u2, springBoot, "PROFICIENT", "ADVANCED");

        when(userSkillRepository.findByUserId(1L)).thenReturn(List.of(s1));
        when(userSkillRepository.findByUserId(2L)).thenReturn(List.of(s2));

        CompatibilityResponse response = compatibilityService.calculateCompatibility(1L, 2L);

        assertNotNull(response);
        assertEquals(2L, response.getTargetUserId());
        assertTrue(response.getScore() >= 70, "Complementary frontend + backend developers should have high synergy score");
        assertTrue(response.getMatchExplanation().contains("Complementary full-stack pairing") || response.getMatchExplanation().contains("project fit"));
        assertTrue(response.getProjectChemistrySuggestion().contains("Full-Stack"));
    }
}
