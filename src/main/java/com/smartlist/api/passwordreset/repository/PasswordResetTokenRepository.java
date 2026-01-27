package com.smartlist.api.passwordreset.repository;

import com.smartlist.api.passwordreset.model.PasswordResetToken;
import com.smartlist.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(UUID token);
    long countByEmailAndCreatedAtAfter(String email, Instant createdAfter);
    long countByRequestIpAndCreatedAtAfter(String requestIp, Instant createdAfter);

    @Modifying
    @Query("""
            UPDATE PasswordResetToken prt
            SET prt.status = PasswordResetTokenStatus.EXPIRED
            WHERE prt.user = :user
            AND prt.status = PasswordResetTokenStatus.PENDING
    """)
    void expireAllPendingByUser(User user);
}
