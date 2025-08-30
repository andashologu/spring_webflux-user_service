package com.trademarket.api.security.users.service;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trademarket.api.security.generics.CustomRepository;
import com.trademarket.api.security.generics.CustomValidation;
import com.trademarket.api.security.users.exception.UserNotFoundException;
import com.trademarket.api.security.users.model.UserEntity;
import com.trademarket.api.security.users.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CustomValidation<UserEntity> customValidation;
    private final CustomRepository<UserEntity, Long> customRepository;//the way class type is passed for the log we could practice the same
    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, CustomValidation<UserEntity> customValidation, CustomRepository<UserEntity, Long> customRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customValidation = customValidation;
        this.customRepository = customRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public Mono<UserEntity> saveUser(UserEntity userEntity) { 
        return Mono.fromCallable(() -> {
                    if (userEntity.getUsername() != null) userEntity.setUsername(userEntity.getUsername().toLowerCase());
                    if (userEntity.getEmail() != null) userEntity.setEmail(userEntity.getEmail().toLowerCase());
                    log.info("Validating user fields");
                    customValidation.validateAll(userEntity);
                    log.info("Encoding password");
                    userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
                    Instant now = Instant.now();
                    if (userEntity.getId() == null) userEntity.setCreatedAt(now);
                    userEntity.setUpdatedAt(now);
                    userEntity.setAccessedAt(now);
                    return userEntity;
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(validatedUserEntity -> {
                    if (validatedUserEntity.getId() == null) {
                        log.info("Persisting new user");
                        return userRepository.save(validatedUserEntity);
                    } else {
                        log.info("Updating existing user with id ", validatedUserEntity.getId());
                        return userRepository.findById(validatedUserEntity.getId())
                            .switchIfEmpty(Mono.error(new UserNotFoundException("User with id " + validatedUserEntity.getId() + " not found")))
                            .flatMap(_ -> { return userRepository.save(validatedUserEntity); });
                    }
                });
    }

    public Mono<Object> updateUser(Long id, Map<String, Object> updates) {
        updates.put("updatedAt", Instant.now());
        updates.put("accessedAt", Instant.now());
        log.info("Verifying user exists with id " + id);
        return userRepository.findById(id)
            //.switchIfEmpty(Mono.error(new ValidationException(Map.of("id", "User not found"))))
            .switchIfEmpty(Mono.error(new UserNotFoundException("User with id " + id + " not found")))
            .flatMap(existingUser -> 
                Mono.fromCallable(() -> {
                    log.info("Validating user fields");
                    customValidation.validate(new UserEntity(), updates);
                    return existingUser;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(_ -> {
                    if (updates.containsKey("password")) {
                        Object passwordObj = updates.get("password");
                        if (passwordObj instanceof String string) {
                            String password = string.trim();
                            if (!password.isEmpty()) {
                                log.info("Encoding password");
                                String encodedPassword = passwordEncoder.encode(password);
                                updates.put("password", encodedPassword);
                            }
                        } 
                    }
                    log.info("Updating user fields");
                    return customRepository.updateFields(id, updates, UserEntity.class, existingUser);
                })
            );
    }
}