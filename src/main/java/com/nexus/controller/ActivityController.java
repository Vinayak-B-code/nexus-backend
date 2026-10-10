package com.nexus.controller;

import com.nexus.dto.ActivityBreakdownDto;
import com.nexus.dto.ActivityLogDto;
import com.nexus.dto.ActivitySummaryDto;
import com.nexus.dto.ActivityTrendDto;
import com.nexus.entity.User;
import com.nexus.service.ActivityLogService;
import com.nexus.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityLogService activityLogService;
    private final UserService userService;

    public ActivityController(ActivityLogService activityLogService, UserService userService) {
        this.activityLogService = activityLogService;
        this.userService = userService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ActivitySummaryDto> getSummary(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(activityLogService.getSummary(user.getId()));
    }

    @GetMapping("/trend")
    public ResponseEntity<ActivityTrendDto> getTrend(@RequestParam(defaultValue = "WEEKLY") String range,
                                                     Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(activityLogService.getTrend(user.getId(), range));
    }

    @GetMapping("/breakdown")
    public ResponseEntity<ActivityBreakdownDto> getBreakdown(@RequestParam(defaultValue = "ALL") String range,
                                                             Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(activityLogService.getBreakdown(user.getId(), range));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityLogDto>> getRecent(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "15") int size,
                                                         Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(activityLogService.getRecentActivities(user.getId(), page, size));
    }
}
