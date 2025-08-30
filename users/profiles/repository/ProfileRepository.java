package com.trademarket.api.security.users.profiles.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trademarket.api.security.users.model.UserEntity;
import com.trademarket.api.security.users.profiles.model.ProfileEntity;

import reactor.core.publisher.Mono;

@Repository
public interface ProfileRepository extends R2dbcRepository<ProfileEntity, Long> {

    @Query("SELECT * FROM profiles WHERE user_id = :userId")
    Mono<UserEntity> findByUserId(@Param("userId") Long userId);

    @Query("SELECT id FROM profiles WHERE user_id = :userid")
    Mono<ProfileEntity> findIdByUserId(Long userId);
}
