package com.smartlist.api.auth.controller;

import com.smartlist.api.auth.dto.LoginDTO;
import com.smartlist.api.auth.dto.TokenResponseDTO;
import com.smartlist.api.auth.service.AuthService;
import com.smartlist.api.exceptions.InvalidCredentialsException;
import com.smartlist.api.shared.dto.ApiResponse;
import com.smartlist.api.user.repository.UserRepository;
import com.smartlist.api.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> login(@RequestBody @Valid LoginDTO loginDTO, HttpServletResponse response) {
        authService.authenticate(loginDTO);

        String accessToken = authService.createAccessToken(loginDTO.email());
        String refreshToken = authService.createRefreshToken(loginDTO.email());

        User user = userRepository.findByEmail(loginDTO.email()).orElseThrow(() -> new InvalidCredentialsException("008", "Credenciais inválidas"));

        authService.saveRefreshToken(refreshToken, user);

        String cookieHeader = String.format("refreshToken=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Lax",
                refreshToken, 7 * 24 * 60 * 60);

        response.setHeader("Set-Cookie", cookieHeader);

        return ResponseEntity.ok(new ApiResponse<>(true, "Login efetuado com sucesso.", new TokenResponseDTO(accessToken)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = authService.refreshToken(request, response);
        return ResponseEntity.ok(new ApiResponse<>(true, "Token atualizado com sucesso.", new TokenResponseDTO(newAccessToken)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(new ApiResponse<>(true, "Logout efetuado com sucesso.", null));
    }
}
