package com.trademarket.api.security.users.validation;

import org.springframework.stereotype.Component;

import com.trademarket.api.security.users.repository.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueMobileNumberConstraint implements ConstraintValidator<UniqueMobileNumber, String> {

    private final UserRepository userRepository;

    public UniqueMobileNumberConstraint(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String mobileNumber, ConstraintValidatorContext context) {
        if (mobileNumber == null || mobileNumber.isBlank()) return true;

        return !userRepository.findByMobileNumber(mobileNumber)
            .hasElement()
            .blockOptional()
            .orElse(false);
    }
}

