package com.smartlist.api.user.service;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.smartlist.api.exceptions.WeakPasswordException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PasswordStrengthService {
    private final Zxcvbn zxcvbn = new Zxcvbn();

    public void validatePasswordStrength(String password) {
        Strength strength = zxcvbn.measure(password);

        if (strength.getScore() < 3) {
            String warning = strength.getFeedback().getWarning();
            throw new WeakPasswordException("S5001", "Senha fraca: " + (warning != null ? warning : "Escolha uma senha mais forte."));
        }
    }
}
