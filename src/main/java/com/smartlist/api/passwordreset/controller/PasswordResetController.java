package com.smartlist.api.passwordreset.controller;

import com.smartlist.api.passwordreset.dto.PasswordExchangeDTO;
import com.smartlist.api.passwordreset.dto.PasswordResetRequestDTO;
import com.smartlist.api.passwordreset.service.PasswordResetService;
import com.smartlist.api.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/password-resets")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDTO request) {
        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Se o email existir em nosso sistema, instruções serão enviadas.", null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Void>> validatePasswordResetToken(@RequestParam UUID token) {
        passwordResetService.validateToken(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Token válido.", null));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid PasswordExchangeDTO passwordExchangeDTO) {
        passwordResetService.resetPassword(passwordExchangeDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Redefinição de senha realizada com sucesso.", null));
    }
}
