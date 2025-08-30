package com.trademarket.api.security.users.profiles.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.trademarket.api.exceptions.ValidationException;
import com.trademarket.api.security.users.profiles.model.ProfileEntity;
import com.trademarket.api.security.users.profiles.repository.ProfileRepository;
import com.trademarket.api.security.users.profiles.service.ProfileService;
import com.trademarket.api.security.users.repository.UserRepository;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileController(ProfileService profileService, ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Mono<ProfileEntity> createProfile(@RequestBody ProfileEntity profileEntity) {
        System.out.println("profileEntity: " + profileEntity);
        Instant now = Instant.now();
        // Check userId is provided
        if (profileEntity.getUserId() == null) return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID must be provided"));
        return userRepository.findById(profileEntity.getUserId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + profileEntity.getUserId())))
            .flatMap(_ -> {
                if (profileEntity.getId() == null) {
                    // CREATE
                    profileEntity.setCreatedAt(now);
                    profileEntity.setUpdatedAt(now);
                    profileEntity.setAccessedAt(now);
                    return profileService.save(profileEntity);
                } else {
                    // UPDATE
                    return profileRepository.findById(profileEntity.getId())
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found with id: " + profileEntity.getId())))
                        .flatMap(_ -> {
                            profileEntity.setUpdatedAt(now);
                            profileEntity.setAccessedAt(now);
                            return profileService.save(profileEntity);
                        });
                }
            });
    }


    @PatchMapping("/{user_id}")
    public Mono<ResponseEntity<Object>> updateProfile(@PathVariable Long user_id, @RequestBody Map<String, Object> updates) {
        return profileRepository.findIdByUserId(user_id)
            .flatMap(profileEntity -> 
                profileService.updateProfile(profileEntity, updates)
                    .map(updatedProfile -> ResponseEntity.ok((Object) updatedProfile)))
            .onErrorResume(ValidationException.class, ex -> {
                return Mono.just(ResponseEntity.badRequest().body(ex.getErrors()));})
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")));
    }
}