package com.trademarket.api.security.users.validation;

import org.springframework.stereotype.Component;

import com.trademarket.api.security.users.repository.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueUsernameConstraint implements ConstraintValidator<UniqueUsername, String> {

    private final UserRepository userRepository;

    public UniqueUsernameConstraint(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) return true;

        return !userRepository.findByUsername(username)
            .hasElement()
            .blockOptional()
            .orElse(false);
    }
}

