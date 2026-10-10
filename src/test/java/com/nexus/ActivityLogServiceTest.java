package com.nexus;

import com.nexus.dto.ActivityBreakdownDto;
import com.nexus.dto.ActivityLogDto;
import com.nexus.dto.ActivitySummaryDto;
import com.nexus.dto.ActivityTrendDto;
import com.nexus.entity.ActivityLog;
import com.nexus.entity.User;
import com.nexus.repository.*;
import com.nexus.service.ActivityLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ActivityLogServiceTest {

    private ActivityLogRepository activityLogRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private CircleMemberRepository circleMemberRepository;
    private UserSkillRepository userSkillRepository;
    private ProjectCommentRepository commentRepository;
    private AchievementRepository achievementRepository;
    private SkillBountyRepository bountyRepository;
    private ConnectionRepository connectionRepository;

    private ActivityLogService activityLogService;

    @BeforeEach
    void setUp() {
        activityLogRepository = Mockito.mock(ActivityLogRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
        circleMemberRepository = Mockito.mock(CircleMemberRepository.class);
        userSkillRepository = Mockito.mock(UserSkillRepository.class);
        commentRepository = Mockito.mock(ProjectCommentRepository.class);
        achievementRepository = Mockito.mock(AchievementRepository.class);
        bountyRepository = Mockito.mock(SkillBountyRepository.class);
        connectionRepository = Mockito.mock(ConnectionRepository.class);

        activityLogService = new ActivityLogService(
                activityLogRepository,
                projectRepository,
                projectMemberRepository,
                circleMemberRepository,
                userSkillRepository,
                commentRepository,
                achievementRepository,
                bountyRepository,
                connectionRepository
        );
    }

    @Test
    void testLogActivitySavesEntity() {
        User user = new User("Ananya Patel", "ananya@bits.ac.in", "pass", "BITS Pilani", "CS", "Year 2", "Builder");
        user.setId(10L);

        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(i -> i.getArgument(0));

        ActivityLog saved = activityLogService.logActivity(user, "PROJECT_CREATE", "Created project: Nexus Hub");

        assertNotNull(saved);
        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository, times(1)).save(captor.capture());

        ActivityLog captured = captor.getValue();
        assertEquals("PROJECT_CREATE", captured.getActivityType());
        assertEquals("Created project: Nexus Hub", captured.getDescription());
        assertEquals(user, captured.getUser());
        assertNotNull(captured.getCreatedAt());
    }

    @Test
    void testGetSummaryAggregatesCounts() {
        when(activityLogRepository.countByUserId(10L)).thenReturn(42L);
        when(projectRepository.findByCreatorIdOrderByCreatedAtDesc(10L)).thenReturn(Collections.emptyList());
        when(projectMemberRepository.findAll()).thenReturn(Collections.emptyList());
        when(circleMemberRepository.findByUserId(10L)).thenReturn(Collections.emptyList());
        when(userSkillRepository.findByUserId(10L)).thenReturn(Collections.emptyList());
        when(commentRepository.countByUserId(10L)).thenReturn(9L);
        when(achievementRepository.countByUserId(10L)).thenReturn(3L);
        when(bountyRepository.findByCreatorIdOrderByCreatedAtDesc(10L)).thenReturn(Collections.emptyList());
        when(connectionRepository.findAcceptedConnections(10L)).thenReturn(Collections.emptyList());

        ActivitySummaryDto summary = activityLogService.getSummary(10L);

        assertNotNull(summary);
        assertEquals(42L, summary.getTotalActivities());
        assertEquals(9L, summary.getCommentsPosted());
        assertEquals(3L, summary.getAchievementsAdded());
    }

    @Test
    void testWeeklyTrendPreservesZeroDateBuckets() {
        when(activityLogRepository.findActivitiesInRange(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ActivityTrendDto trend = activityLogService.getTrend(10L, "WEEKLY");

        assertEquals("WEEKLY", trend.getRange());
        assertEquals(7, trend.getLabels().size(), "Weekly range must yield exactly 7 day buckets");
        assertEquals(7, trend.getDataPoints().size(), "Weekly range must yield exactly 7 data points");
        assertEquals(0L, trend.getTotalInRange());

        for (Long count : trend.getDataPoints()) {
            assertEquals(0L, count, "Zero activity days must preserve count 0");
        }
    }

    @Test
    void testMonthlyTrendPreservesThirtyDateBuckets() {
        when(activityLogRepository.findActivitiesInRange(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ActivityTrendDto trend = activityLogService.getTrend(10L, "MONTHLY");

        assertEquals("MONTHLY", trend.getRange());
        assertEquals(30, trend.getLabels().size(), "Monthly range must yield exactly 30 day buckets");
        assertEquals(30, trend.getDataPoints().size());
    }

    @Test
    void testYearlyTrendPreservesTwelveMonthBuckets() {
        when(activityLogRepository.findActivitiesInRange(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ActivityTrendDto trend = activityLogService.getTrend(10L, "YEARLY");

        assertEquals("YEARLY", trend.getRange());
        assertEquals(12, trend.getLabels().size(), "Yearly range must yield exactly 12 month buckets");
        assertEquals(12, trend.getDataPoints().size());
    }

    @Test
    void testGetBreakdownReturnsAllCategories() {
        Object[] row1 = new Object[]{"PROJECT_CREATE", 4L};
        Object[] row2 = new Object[]{"PROJECT_COMMENT", 2L};
        Object[] row3 = new Object[]{"SKILL_ADD", 6L};

        when(activityLogRepository.countActivitiesByType(10L)).thenReturn(List.of(row1, row2, row3));

        ActivityBreakdownDto breakdown = activityLogService.getBreakdown(10L, "ALL");

        assertNotNull(breakdown);
        assertEquals("ALL", breakdown.getRange());
        assertEquals(12L, breakdown.getTotal());
        assertTrue(breakdown.getItems().size() >= 7, "Should include all standard activity categories");
    }

    @Test
    void testGetRecentActivitiesPagination() {
        User user = new User("Ananya Patel", "ananya@bits.ac.in", "pass", "BITS Pilani", "CS", "Year 2", "Bio");
        user.setId(10L);
        ActivityLog log = new ActivityLog(user, "PROJECT_CREATE", "Created project", LocalDateTime.now());
        log.setId(100L);

        Page<ActivityLog> page = new PageImpl<>(List.of(log));
        when(activityLogRepository.findByUserIdOrderByCreatedAtDesc(eq(10L), any(Pageable.class))).thenReturn(page);

        List<ActivityLogDto> recent = activityLogService.getRecentActivities(10L, 0, 10);

        assertEquals(1, recent.size());
        assertEquals("PROJECT_CREATE", recent.get(0).getActivityType());
        assertEquals("Projects", recent.get(0).getCategory());
        assertEquals("Created project", recent.get(0).getDescription());
    }
}
