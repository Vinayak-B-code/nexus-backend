package com.nexus.controller;

import com.nexus.dto.LoginRequest;
import com.nexus.dto.RegisterRequest;
import com.nexus.dto.UserResponse;
import com.nexus.entity.User;
import com.nexus.exception.UnauthorizedException;
import com.nexus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final com.nexus.service.ActivityLogService activityLogService;
    private final org.springframework.security.web.context.SecurityContextRepository securityContextRepository;

    public AuthController(UserService userService, 
                          AuthenticationManager authenticationManager, 
                          com.nexus.service.ActivityLogService activityLogService,
                          org.springframework.security.web.context.SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.activityLogService = activityLogService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, 
                                      HttpServletRequest httpRequest, 
                                      jakarta.servlet.http.HttpServletResponse httpResponse) {
        User user = userService.registerUser(request);

        // Auto login upon registration
        authenticateAndSetSession(request.getEmail(), request.getPassword(), httpRequest, httpResponse);

        activityLogService.logActivity(user, "USER_REGISTER", "Joined NEXUS builder community");

        UserResponse userResponse = userService.toUserResponse(user);
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Registration successful");
        body.put("user", userResponse);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, 
                                   HttpServletRequest httpRequest, 
                                   jakarta.servlet.http.HttpServletResponse httpResponse) {
        Authentication authentication = authenticateAndSetSession(request.getEmail(), request.getPassword(), httpRequest, httpResponse);
        User user = userService.findByEmail(authentication.getName());

        activityLogService.logActivity(user, "USER_LOGIN", "Signed in to NEXUS platform");

        UserResponse userResponse = userService.toUserResponse(user);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Login successful");
        body.put("user", userResponse);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            Map<String, Object> body = new HashMap<>();
            body.put("authenticated", false);
            body.put("message", "Not authenticated");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(body);
        }

        User user = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(userService.toUserResponse(user));
    }

    private Authentication authenticateAndSetSession(String email, String password, 
                                                    HttpServletRequest httpRequest, 
                                                    jakarta.servlet.http.HttpServletResponse httpResponse) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email.trim().toLowerCase(), password);

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return authentication;
    }
}
