package com.trademarket.api.security.users.profiles.validation;

import org.springframework.stereotype.Component;

import com.trademarket.api.security.users.profiles.repository.ProfileRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueUserIdConstraint implements ConstraintValidator<UniqueUserId, Long> {

    private final ProfileRepository profileRepository;

    public UniqueUserIdConstraint(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext context) {
        if (userId == null) return true;
        
        return !profileRepository.findByUserId(userId)
                                .hasElement()
                                .blockOptional()
                                .orElse(false);
    }
}

