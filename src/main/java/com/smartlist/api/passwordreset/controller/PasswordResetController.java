package com.smartlist.api.passwordreset.controller;

import com.smartlist.api.passwordreset.dto.PasswordExchangeDTO;
import com.smartlist.api.passwordreset.dto.PasswordResetRequestDTO;
import com.smartlist.api.passwordreset.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/reset-password")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDTO request) {
        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.ok("Se o email existir em nosso sistema, instruções serão enviadas.");
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validatePasswordResetToken(@RequestParam UUID token) {
        passwordResetService.validateToken(token);
        return ResponseEntity.ok("Token válido.");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid PasswordExchangeDTO passwordExchangeDTO) {
        passwordResetService.resetPassword(passwordExchangeDTO);
        return ResponseEntity.ok("Redefinição de senha realizada com sucesso.");
    }
}
