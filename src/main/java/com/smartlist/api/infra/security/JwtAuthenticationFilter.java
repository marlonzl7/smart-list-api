package com.smartlist.api.security;

import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import com.smartlist.api.userdetails.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String jwt = authHeader.substring(7);

            if (jwtUtils.isValidToken(jwt)) {
                String username = jwtUtils.getUsernameFromToken(jwt);
                User user = userRepository.findByEmail(username).orElseThrow(() -> {
                    log.error("Usuário não encontrado: " + username);
                    return new RuntimeException("Usuário não encontrado: " + username);
                });

                UserDetailsImpl userDetails = new UserDetailsImpl(user);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Usuário autenticado via JWT: {}", username);
            } else {
                log.warn("Token JWT inválido recebido no header Authorization");
            }
        }

        filterChain.doFilter(request, response);
    }
}
