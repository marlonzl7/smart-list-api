package com.smartlist.api.passwordreset.repository;

import com.smartlist.api.passwordreset.model.PasswordResetToken;
import com.smartlist.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(UUID token);
    long countByEmailAndCreatedAtAfter(String email, Instant createdAfter);
    long countByRequestIpAndCreatedAtAfter(String requestIp, Instant createdAfter);
    void expireAllPendingByUser(User user);
}
