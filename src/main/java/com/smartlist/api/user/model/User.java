package com.smartlist.api.user.model;

import com.smartlist.api.user.enums.NotificationPreference;
import com.smartlist.api.user.enums.ThemePreference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_preference")
    private NotificationPreference notificationPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme_preference")
    private ThemePreference themePreference;

    @Column(name = "critical_quantity_days")
    private Integer criticalQuantityDays;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
