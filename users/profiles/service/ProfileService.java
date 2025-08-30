package com.trademarket.api.security.users.profiles.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import com.trademarket.api.security.generics.CustomRepository;
import com.trademarket.api.security.generics.CustomValidation;
import com.trademarket.api.security.generics.conversion.JsonConversion;
import com.trademarket.api.security.users.profiles.model.Preferences;
import com.trademarket.api.security.users.profiles.model.ProfileEntity;
import com.trademarket.api.security.users.profiles.model.Settings;

import io.r2dbc.postgresql.codec.Json;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ProfileService {

    private final DatabaseClient databaseClient;
    private final CustomValidation<ProfileEntity> customValidation;
    private final CustomRepository<ProfileEntity, Long> customRepository;

    public ProfileService(DatabaseClient databaseClient, CustomValidation<ProfileEntity> customValidation, CustomRepository<ProfileEntity, Long> customRepository) {
        this.databaseClient = databaseClient;
        this.customValidation = customValidation;
        this.customRepository = customRepository;
    }
 
    public Mono<ProfileEntity> save(ProfileEntity profile) {
        return Mono.fromCallable(() -> {
               customValidation.validateAll(profile);
                return profile;
            }).subscribeOn(Schedulers.boundedElastic())
            .then(
                bindValuesToSpec(databaseClient.sql("""
                        INSERT INTO profiles (
                            user_id, firstname, lastname, profile_picture, bio, website, preferences, settings, created_at, updated_at, accessed_at
                        ) VALUES (
                            :user_id, :firstname, :lastname, :profile_picture, :bio, :website, :preferences, :settings, :created_at, :updated_at, :accessed_at
                        )
                        RETURNING id, user_id, firstname, lastname, profile_picture, bio, website, preferences, settings, created_at, updated_at, accessed_at
                    """), profile)
                    .map((row, _) -> {
                        ProfileEntity savedProfileEntity = new ProfileEntity(
                            row.get("id", Long.class),
                            row.get("user_id", Long.class),
                            row.get("firstname", String.class),
                            row.get("lastname", String.class),
                            row.get("profile_picture", String.class),
                            row.get("bio", String.class),
                            row.get("website", String.class),
                            JsonConversion.jsonToObject(row.get("preferences", Json.class), Preferences.class),
                            JsonConversion.jsonToObject(row.get("settings", Json.class), Settings.class)
                        );
                        savedProfileEntity.setCreatedAt(row.get("created_at", Instant.class));
                        savedProfileEntity.setUpdatedAt(row.get("updated_at", Instant.class));
                        savedProfileEntity.setAccessedAt(row.get("accessed_at", Instant.class));
                        return savedProfileEntity;
                    })
                    .one()
            );
    }
    
    private DatabaseClient.GenericExecuteSpec bindValuesToSpec(DatabaseClient.GenericExecuteSpec spec, ProfileEntity profile) {
        spec = bindOrNull(spec, "user_id", profile.getUserId(), Long.class);
        spec = bindOrNull(spec, "firstname", profile.getFirstname(), String.class);
        spec = bindOrNull(spec, "lastname", profile.getLastname(), String.class);
        spec = bindOrNull(spec, "profile_picture", profile.getProfilePicture(), String.class);
        spec = bindOrNull(spec, "bio", profile.getBio(), String.class);
        spec = bindOrNull(spec, "website", profile.getWebsite(), String.class);
        spec = bindOrNull(spec, "preferences", JsonConversion.objectToJson(profile.getPreferences()), Json.class);
        spec = bindOrNull(spec, "settings", JsonConversion.objectToJson(profile.getSettings()), Json.class);
        spec = bindOrNull(spec, "created_at", profile.getCreatedAt(), Instant.class);
        spec = bindOrNull(spec, "updated_at", profile.getUpdatedAt(), Instant.class);
        spec = bindOrNull(spec, "accessed_at", profile.getAccessedAt(), Instant.class);
        return spec;
    }
    
    private <T> DatabaseClient.GenericExecuteSpec bindOrNull(DatabaseClient.GenericExecuteSpec spec, String key, T value, Class<T> type) {
        return value != null ? spec.bind(key, value) : spec.bindNull(key, type);
    }

    public Mono<Object> updateProfile(ProfileEntity profileEntity, Map<String, Object> updates) {
        updates.put("updatedAt", Instant.now());
        updates.put("accessedAt", Instant.now());
        return Mono.fromCallable(() -> {
                customValidation.validate(profileEntity, updates);
                return profileEntity;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(validatedProfile -> {
                Long id = validatedProfile.getId(); 
                if (id == null) return Mono.error(new IllegalArgumentException("Profile ID cannot be null for update"));
                return customRepository.updateFields(id, updates, ProfileEntity.class, validatedProfile);
            });
    }
    
}