package com.smartlist.api.infra.security;

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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
                User user = userRepository.findByEmail(username).orElse(null);
                if (user == null) {
                    log.warn(
                            "JWT inválido. IP={}, Method={}, URI={}",
                            request.getRemoteAddr(),
                            request.getMethod(),
                            request.getRequestURI()
                    );
                    filterChain.doFilter(request, response);
                    return;
                }

                UserDetailsImpl userDetails = new UserDetailsImpl(user);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug(
                        "Usuário autenticado via JWT. UserId={}, Email={}",
                        user.getUserId(),
                        user.getEmail()
                );
            } else {
                log.warn(
                        "Falha de autenticação JWT. IP={}, Method={}, URI={}",
                        request.getRemoteAddr(),
                        request.getMethod(),
                        request.getRequestURI()
                );
            }
        }

        filterChain.doFilter(request, response);
    }
}
