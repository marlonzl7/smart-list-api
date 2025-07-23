package com.smartlist.api.passwordreset.model;

import com.smartlist.api.passwordreset.enums.PasswordResetTokenStatus;
import com.smartlist.api.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passwordResetTokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(unique = true, nullable = false, columnDefinition = "UUID")
    private UUID token;

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PasswordResetTokenStatus status;

    @Column(name = "request_ip", nullable = false)
    private String requestIp;

    @Column(name = "user_agent", nullable = false)
    private String userAgent;
}
