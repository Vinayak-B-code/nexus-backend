package com.nexus.controller;

import com.nexus.dto.ConnectionDto;
import com.nexus.entity.User;
import com.nexus.service.ConnectionService;
import com.nexus.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final UserService userService;

    public ConnectionController(ConnectionService connectionService, UserService userService) {
        this.connectionService = connectionService;
        this.userService = userService;
    }

    @PostMapping("/request/{targetUserId}")
    public ResponseEntity<?> sendRequest(@PathVariable Long targetUserId, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        connectionService.sendRequest(user.getId(), targetUserId);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Connection request sent");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{connectionId}/respond")
    public ResponseEntity<?> respondToRequest(@PathVariable Long connectionId,
                                              @RequestParam boolean accept,
                                              Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        connectionService.respondToRequest(connectionId, user.getId(), accept);

        Map<String, String> res = new HashMap<>();
        res.put("message", accept ? "Connection accepted" : "Connection declined");
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<List<ConnectionDto>> getMyConnections(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(connectionService.getMyConnections(user.getId()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ConnectionDto>> getPendingReceived(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(connectionService.getPendingReceived(user.getId()));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<ConnectionDto>> getPendingSent(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(connectionService.getPendingSent(user.getId()));
    }

    @GetMapping("/status/{otherUserId}")
    public ResponseEntity<Map<String, String>> getConnectionStatus(@PathVariable Long otherUserId,
                                                                   Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        String status = connectionService.getConnectionStatus(user.getId(), otherUserId);
        Map<String, String> res = new HashMap<>();
        res.put("status", status);
        return ResponseEntity.ok(res);
    }
}
