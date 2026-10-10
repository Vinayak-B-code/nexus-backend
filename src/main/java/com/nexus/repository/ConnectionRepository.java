package com.nexus.repository;

import com.nexus.entity.Connection;
import com.nexus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    @Query("SELECT c FROM Connection c WHERE (c.requester = :u1 AND c.recipient = :u2) OR (c.requester = :u2 AND c.recipient = :u1)")
    Optional<Connection> findBetweenUsers(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT c FROM Connection c WHERE (c.requester.id = :userId OR c.recipient.id = :userId) AND c.status = 'ACCEPTED'")
    List<Connection> findAcceptedConnections(@Param("userId") Long userId);

    @Query("SELECT c FROM Connection c WHERE c.recipient.id = :userId AND c.status = 'PENDING' ORDER BY c.createdAt DESC")
    List<Connection> findPendingReceived(@Param("userId") Long userId);

    @Query("SELECT c FROM Connection c WHERE c.requester.id = :userId AND c.status = 'PENDING' ORDER BY c.createdAt DESC")
    List<Connection> findPendingSent(@Param("userId") Long userId);
}
