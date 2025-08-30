package com.trademarket.api.security.users.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trademarket.api.security.users.model.UserEntity;

import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<UserEntity, Long> {

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username)")
    Mono<UserEntity> findByUsername(@Param("username") String username);
    
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email)")
    Mono<UserEntity> findByEmail(@Param("email") String email);

    @Query("SELECT * FROM users WHERE mobile_number = :mobileNumber")
    Mono<UserEntity> findByMobileNumber(@Param("mobileNumber") String mobileNumber);

    @Query("""
        SELECT * FROM users 
        WHERE LOWER(username) = LOWER(:identifier) 
        OR LOWER(email) = LOWER(:identifier) 
        OR mobile_number = :identifier
    """)
    Mono<UserEntity> findByUsernameOrEmailOrMobileNumber(@Param("identifier") String identifier);

    Mono<Void> deleteByUsername(String username);

}