package com.nexus.service;

import com.nexus.dto.ActivityBreakdownDto;
import com.nexus.dto.ActivityLogDto;
import com.nexus.dto.ActivitySummaryDto;
import com.nexus.dto.ActivityTrendDto;
import com.nexus.entity.ActivityLog;
import com.nexus.entity.User;
import com.nexus.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserSkillRepository userSkillRepository;
    private final ProjectCommentRepository commentRepository;
    private final AchievementRepository achievementRepository;
    private final SkillBountyRepository bountyRepository;
    private final ConnectionRepository connectionRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository,
                              ProjectRepository projectRepository,
                              ProjectMemberRepository projectMemberRepository,
                              CircleMemberRepository circleMemberRepository,
                              UserSkillRepository userSkillRepository,
                              ProjectCommentRepository commentRepository,
                              AchievementRepository achievementRepository,
                              SkillBountyRepository bountyRepository,
                              ConnectionRepository connectionRepository) {
        this.activityLogRepository = activityLogRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.circleMemberRepository = circleMemberRepository;
        this.userSkillRepository = userSkillRepository;
        this.commentRepository = commentRepository;
        this.achievementRepository = achievementRepository;
        this.bountyRepository = bountyRepository;
        this.connectionRepository = connectionRepository;
    }

    public ActivityLog logActivity(User user, String activityType, String description) {
        if (user == null || activityType == null) return null;
        ActivityLog log = new ActivityLog(user, activityType, description, LocalDateTime.now());
        return activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public ActivitySummaryDto getSummary(Long userId) {
        long totalActivities = activityLogRepository.countByUserId(userId);
        long projectsCreated = projectRepository.findByCreatorIdOrderByCreatedAtDesc(userId).size();
        long projectCollabs = projectMemberRepository.findAll().stream()
                .filter(m -> m.getUser().getId().equals(userId) && !"Project Lead".equalsIgnoreCase(m.getRole()))
                .count();
        long circleMemberships = circleMemberRepository.findByUserId(userId).size();
        long skillsAdded = userSkillRepository.findByUserId(userId).size();
        long comments = commentRepository.countByUserId(userId);
        long achievements = achievementRepository.countByUserId(userId);
        long bounties = bountyRepository.findByCreatorIdOrderByCreatedAtDesc(userId).size();
        long connections = connectionRepository.findAcceptedConnections(userId).size();

        return new ActivitySummaryDto(
                totalActivities,
                projectsCreated,
                projectCollabs + circleMemberships,
                skillsAdded,
                comments,
                achievements,
                bounties,
                connections
        );
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDto> getRecentActivities(Long userId, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));

        List<ActivityLog> logs = activityLogRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(safePage, safeSize)
        ).getContent();

        List<ActivityLogDto> dtos = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm");

        for (ActivityLog log : logs) {
            String category = categorizeActivityType(log.getActivityType());
            dtos.add(new ActivityLogDto(
                    log.getId(),
                    log.getActivityType(),
                    category,
                    log.getDescription(),
                    log.getCreatedAt(),
                    log.getCreatedAt().format(formatter)
            ));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public ActivityTrendDto getTrend(Long userId, String range) {
        String safeRange = (range != null) ? range.trim().toUpperCase() : "WEEKLY";

        List<String> labels = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<Long> dataPoints = new ArrayList<>();
        long totalInRange = 0;

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MMM dd");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        DateTimeFormatter isoDate = DateTimeFormatter.ISO_LOCAL_DATE;

        LocalDateTime now = LocalDateTime.now();

        if ("YEARLY".equals(safeRange)) {
            // Last 12 months
            YearMonth currentYm = YearMonth.from(now);
            LocalDateTime startDate = currentYm.minusMonths(11).atDay(1).atStartOfDay();
            LocalDateTime endDate = now;

            List<ActivityLog> logs = activityLogRepository.findActivitiesInRange(userId, startDate, endDate);
            Map<YearMonth, Long> countsByMonth = new HashMap<>();
            for (ActivityLog log : logs) {
                YearMonth ym = YearMonth.from(log.getCreatedAt());
                countsByMonth.put(ym, countsByMonth.getOrDefault(ym, 0L) + 1L);
            }

            for (int i = 11; i >= 0; i--) {
                YearMonth ym = currentYm.minusMonths(i);
                long count = countsByMonth.getOrDefault(ym, 0L);
                labels.add(ym.format(monthFormatter));
                dates.add(ym.atDay(1).format(isoDate));
                dataPoints.add(count);
                totalInRange += count;
            }
        } else if ("MONTHLY".equals(safeRange)) {
            // Last 30 days
            LocalDate today = now.toLocalDate();
            LocalDateTime startDate = today.minusDays(29).atStartOfDay();
            LocalDateTime endDate = now;

            List<ActivityLog> logs = activityLogRepository.findActivitiesInRange(userId, startDate, endDate);
            Map<LocalDate, Long> countsByDay = new HashMap<>();
            for (ActivityLog log : logs) {
                LocalDate d = log.getCreatedAt().toLocalDate();
                countsByDay.put(d, countsByDay.getOrDefault(d, 0L) + 1L);
            }

            for (int i = 29; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                long count = countsByDay.getOrDefault(d, 0L);
                labels.add(d.format(dayFormatter));
                dates.add(d.format(isoDate));
                dataPoints.add(count);
                totalInRange += count;
            }
        } else {
            // WEEKLY: Last 7 days
            safeRange = "WEEKLY";
            LocalDate today = now.toLocalDate();
            LocalDateTime startDate = today.minusDays(6).atStartOfDay();
            LocalDateTime endDate = now;

            List<ActivityLog> logs = activityLogRepository.findActivitiesInRange(userId, startDate, endDate);
            Map<LocalDate, Long> countsByDay = new HashMap<>();
            for (ActivityLog log : logs) {
                LocalDate d = log.getCreatedAt().toLocalDate();
                countsByDay.put(d, countsByDay.getOrDefault(d, 0L) + 1L);
            }

            DateTimeFormatter weekdayFormatter = DateTimeFormatter.ofPattern("EEE (MMM d)");
            for (int i = 6; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                long count = countsByDay.getOrDefault(d, 0L);
                labels.add(d.format(weekdayFormatter));
                dates.add(d.format(isoDate));
                dataPoints.add(count);
                totalInRange += count;
            }
        }

        return new ActivityTrendDto(safeRange, labels, dates, dataPoints, totalInRange);
    }

    @Transactional(readOnly = true)
    public ActivityBreakdownDto getBreakdown(Long userId, String range) {
        String safeRange = (range != null) ? range.trim().toUpperCase() : "ALL";
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> typeCounts;

        if ("WEEKLY".equals(safeRange)) {
            LocalDateTime start = now.minusDays(7);
            typeCounts = activityLogRepository.countActivitiesByTypeInRange(userId, start, now);
        } else if ("MONTHLY".equals(safeRange)) {
            LocalDateTime start = now.minusDays(30);
            typeCounts = activityLogRepository.countActivitiesByTypeInRange(userId, start, now);
        } else if ("YEARLY".equals(safeRange)) {
            LocalDateTime start = now.minusMonths(12);
            typeCounts = activityLogRepository.countActivitiesByTypeInRange(userId, start, now);
        } else {
            safeRange = "ALL";
            typeCounts = activityLogRepository.countActivitiesByType(userId);
        }

        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("Projects", 0L);
        categoryCounts.put("Collaborations", 0L);
        categoryCounts.put("Skills", 0L);
        categoryCounts.put("Comments & Likes", 0L);
        categoryCounts.put("Skill Bounties", 0L);
        categoryCounts.put("Academic Achievements", 0L);
        categoryCounts.put("Profile & Community", 0L);

        long total = 0;
        for (Object[] row : typeCounts) {
            String type = (String) row[0];
            long count = ((Number) row[1]).longValue();
            String cat = categorizeActivityType(type);
            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0L) + count);
            total += count;
        }

        Map<String, String> colors = Map.of(
                "Projects", "#625BEE",
                "Collaborations", "#0D9488",
                "Skills", "#2563EB",
                "Comments & Likes", "#E11D48",
                "Skill Bounties", "#D97706",
                "Academic Achievements", "#10B981",
                "Profile & Community", "#7C3AED"
        );

        List<ActivityBreakdownDto.CategoryItem> items = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
            double pct = total > 0 ? Math.round(((double) entry.getValue() / total) * 1000.0) / 10.0 : 0.0;
            items.add(new ActivityBreakdownDto.CategoryItem(
                    entry.getKey(),
                    entry.getValue(),
                    pct,
                    colors.getOrDefault(entry.getKey(), "#64748B")
            ));
        }

        return new ActivityBreakdownDto(safeRange, total, items);
    }

    public String categorizeActivityType(String type) {
        if (type == null) return "Profile & Community";
        String t = type.toUpperCase();
        if (t.contains("PROJECT_CREATE") || t.contains("PROJECT_UPDATE") || t.contains("PROOF_OF_BUILD")) {
            return "Projects";
        }
        if (t.contains("COLLAB") || t.contains("CIRCLE")) {
            return "Collaborations";
        }
        if (t.contains("SKILL_ADD") || t.contains("SKILL_REMOVE") || t.contains("SKILL_UPDATE")) {
            return "Skills";
        }
        if (t.contains("COMMENT") || t.contains("LIKE")) {
            return "Comments & Likes";
        }
        if (t.contains("BOUNTY")) {
            return "Skill Bounties";
        }
        if (t.contains("ACHIEVEMENT")) {
            return "Academic Achievements";
        }
        return "Profile & Community";
    }
}
