package com.nexus.service;

import com.nexus.dto.ConnectionDto;
import com.nexus.entity.Connection;
import com.nexus.entity.User;
import com.nexus.exception.BadRequestException;
import com.nexus.exception.ResourceNotFoundException;
import com.nexus.exception.UnauthorizedException;
import com.nexus.repository.ConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public ConnectionService(ConnectionRepository connectionRepository,
                             UserService userService,
                             NotificationService notificationService) {
        this.connectionRepository = connectionRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public Connection sendRequest(Long requesterId, Long recipientId) {
        if (requesterId.equals(recipientId)) {
            throw new BadRequestException("You cannot connect with yourself");
        }

        User requester = userService.findById(requesterId);
        User recipient = userService.findById(recipientId);

        Optional<Connection> existing = connectionRepository.findBetweenUsers(requester, recipient);
        if (existing.isPresent()) {
            Connection conn = existing.get();
            if ("ACCEPTED".equals(conn.getStatus())) {
                throw new BadRequestException("You are already connected");
            }
            if ("PENDING".equals(conn.getStatus())) {
                throw new BadRequestException("A connection request is already pending between you two");
            }
            // If REJECTED, allow re-requesting
            conn.setRequester(requester);
            conn.setRecipient(recipient);
            conn.setStatus("PENDING");
            conn.setUpdatedAt(LocalDateTime.now());
            Connection saved = connectionRepository.save(conn);

            notificationService.createNotification(
                    recipient,
                    "New Connection Request",
                    requester.getFullName() + " sent you a connection request",
                    "/discover.html?user=" + requester.getId()
            );
            return saved;
        }

        Connection newConn = new Connection(requester, recipient);
        Connection saved = connectionRepository.save(newConn);

        notificationService.createNotification(
                recipient,
                "New Connection Request",
                requester.getFullName() + " sent you a connection request",
                "/discover.html?user=" + requester.getId()
        );

        return saved;
    }

    public void respondToRequest(Long connectionId, Long currentUserId, boolean accept) {
        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (!conn.getRecipient().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the recipient can respond to this request");
        }

        conn.setStatus(accept ? "ACCEPTED" : "REJECTED");
        conn.setUpdatedAt(LocalDateTime.now());
        connectionRepository.save(conn);

        notificationService.createNotification(
                conn.getRequester(),
                accept ? "Connection Accepted!" : "Connection Update",
                accept ? conn.getRecipient().getFullName() + " accepted your connection request" :
                         conn.getRecipient().getFullName() + " declined your connection request",
                "/discover.html?user=" + conn.getRecipient().getId()
        );
    }

    public List<ConnectionDto> getMyConnections(Long userId) {
        return connectionRepository.findAcceptedConnections(userId).stream()
                .map(c -> toDto(c, userId))
                .collect(Collectors.toList());
    }

    public List<ConnectionDto> getPendingReceived(Long userId) {
        return connectionRepository.findPendingReceived(userId).stream()
                .map(c -> toDto(c, userId))
                .collect(Collectors.toList());
    }

    public List<ConnectionDto> getPendingSent(Long userId) {
        return connectionRepository.findPendingSent(userId).stream()
                .map(c -> toDto(c, userId))
                .collect(Collectors.toList());
    }

    public String getConnectionStatus(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) return "SELF";
        User u1 = userService.findById(userId);
        User u2 = userService.findById(otherUserId);
        Optional<Connection> conn = connectionRepository.findBetweenUsers(u1, u2);
        if (conn.isEmpty()) return "NONE";
        Connection c = conn.get();
        if ("ACCEPTED".equals(c.getStatus())) return "CONNECTED";
        if ("PENDING".equals(c.getStatus())) {
            return c.getRequester().getId().equals(userId) ? "REQUESTED" : "PENDING_APPROVAL";
        }
        return "NONE";
    }

    private ConnectionDto toDto(Connection c, Long currentUserId) {
        ConnectionDto dto = new ConnectionDto();
        dto.setId(c.getId());
        dto.setStatus(c.getStatus());
        dto.setCreatedAt(c.getCreatedAt());

        User other = c.getRequester().getId().equals(currentUserId) ? c.getRecipient() : c.getRequester();
        dto.setOtherUserId(other.getId());
        dto.setOtherUserName(other.getFullName());
        dto.setOtherUserCollege(other.getCollege());
        dto.setOtherUserAvatarColor(other.getAvatarColor());
        dto.setOtherUserBio(other.getBio());
        dto.setSentByMe(c.getRequester().getId().equals(currentUserId));
        return dto;
    }
}
