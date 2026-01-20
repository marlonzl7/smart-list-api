package com.smartlist.api.refreshtoken.repository;

import com.smartlist.api.refreshtoken.model.RefreshToken;
import com.smartlist.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.used = true WHERE rt.user = :user")
    void invalidateAllByUser(@Param("user") User user);
}
