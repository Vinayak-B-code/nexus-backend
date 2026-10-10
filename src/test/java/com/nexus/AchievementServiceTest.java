package com.nexus;

import com.nexus.dto.AchievementRequest;
import com.nexus.dto.AchievementResponse;
import com.nexus.entity.Achievement;
import com.nexus.entity.User;
import com.nexus.exception.BadRequestException;
import com.nexus.exception.UnauthorizedException;
import com.nexus.repository.AchievementRepository;
import com.nexus.service.AchievementService;
import com.nexus.service.ActivityLogService;
import com.nexus.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AchievementServiceTest {

    private AchievementRepository achievementRepository;
    private UserService userService;
    private ActivityLogService activityLogService;
    private AchievementService achievementService;

    @BeforeEach
    void setUp() {
        achievementRepository = Mockito.mock(AchievementRepository.class);
        userService = Mockito.mock(UserService.class);
        activityLogService = Mockito.mock(ActivityLogService.class);
        achievementService = new AchievementService(achievementRepository, userService, activityLogService);
    }

    @Test
    void testAddAchievementWithValidHttpUrl() {
        User user = new User("Siddharth Patil", "siddharth@iitb.ac.in", "pass", "IIT Bombay", "Computer Science", "Year 4", "Bio");
        user.setId(5L);
        when(userService.findById(5L)).thenReturn(user);

        AchievementRequest request = new AchievementRequest(
                "AWS Certified Solutions Architect",
                "Certifications",
                "Passed with 920/1000 score",
                "Amazon Web Services",
                LocalDate.of(2026, 5, 20),
                "https://aws.amazon.com/verify/12345",
                true
        );

        when(achievementRepository.save(any(Achievement.class))).thenAnswer(i -> {
            Achievement a = i.getArgument(0);
            a.setId(100L);
            return a;
        });

        AchievementResponse response = achievementService.addAchievement(5L, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("AWS Certified Solutions Architect", response.getTitle());
        assertEquals("Certifications", response.getCategory());
        assertEquals("https://aws.amazon.com/verify/12345", response.getProofUrl());
        assertTrue(response.isPublic());
        assertEquals("SELF_REPORTED", response.getVerificationStatus());

        verify(activityLogService, times(1))
                .logActivity(eq(user), eq("ACHIEVEMENT_CREATE"), contains("AWS Certified Solutions Architect"));
    }

    @Test
    void testAddAchievementRejectsNonHttpUrl() {
        User user = new User("Siddharth Patil", "siddharth@iitb.ac.in", "pass", "IIT Bombay", "Computer Science", "Year 4", "Bio");
        user.setId(5L);
        when(userService.findById(5L)).thenReturn(user);

        AchievementRequest malicious = new AchievementRequest(
                "Hackathon Win",
                "Hackathons",
                "XSS attack probe",
                "Club",
                LocalDate.now(),
                "javascript:alert(document.cookie)",
                true
        );

        assertThrows(BadRequestException.class, () -> achievementService.addAchievement(5L, malicious));
        verify(achievementRepository, never()).save(any());
    }

    @Test
    void testSetVisibility() {
        User user = new User("Siddharth Patil", "siddharth@iitb.ac.in", "pass", "IIT Bombay", "Computer Science", "Year 4", "Bio");
        user.setId(5L);

        Achievement achievement = new Achievement();
        achievement.setId(50L);
        achievement.setUser(user);
        achievement.setTitle("Smart India Hackathon Finalist");
        achievement.setCategory("Hackathons");
        achievement.setPublic(true);

        when(achievementRepository.findById(50L)).thenReturn(Optional.of(achievement));
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(i -> i.getArgument(0));

        AchievementResponse toggled = achievementService.setVisibility(50L, 5L, false);

        assertFalse(toggled.isPublic(), "Visibility should have updated to false");
        verify(activityLogService, times(1)).logActivity(eq(user), eq("ACHIEVEMENT_VISIBILITY"), anyString());
    }

    @Test
    void testSetVisibilityUnauthorizedUser() {
        User user = new User("Siddharth Patil", "siddharth@iitb.ac.in", "pass", "IIT Bombay", "Computer Science", "Year 4", "Bio");
        user.setId(5L);

        Achievement achievement = new Achievement();
        achievement.setId(50L);
        achievement.setUser(user);
        achievement.setTitle("Hackathon");
        achievement.setCategory("Hackathons");

        when(achievementRepository.findById(50L)).thenReturn(Optional.of(achievement));

        assertThrows(UnauthorizedException.class, () -> achievementService.setVisibility(50L, 999L, false));
    }

    @Test
    void testGetPublicAchievementsForUserFiltersPrivate() {
        User user = new User("Kavya Iyer", "kavya@nitt.edu", "pass", "NIT Trichy", "CSE", "Year 3", "Bio");
        user.setId(7L);

        Achievement publicAch = new Achievement();
        publicAch.setId(1L);
        publicAch.setUser(user);
        publicAch.setTitle("Public Honor");
        publicAch.setCategory("Academic accomplishments");
        publicAch.setPublic(true);

        when(achievementRepository.findByUserIdAndIsPublicTrueOrderByAchievementDateDescCreatedAtDesc(7L))
                .thenReturn(List.of(publicAch));

        List<AchievementResponse> publicList = achievementService.getPublicAchievementsForUser(7L);

        assertEquals(1, publicList.size());
        assertEquals("Public Honor", publicList.get(0).getTitle());
        assertTrue(publicList.get(0).isPublic());
    }
}
