package com.trademarket.api.security.users.authorities.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.trademarket.api.security.users.authorities.model.UserRoleEntity;

import reactor.core.publisher.Flux;

@Repository
public interface UserRoleRepository extends R2dbcRepository<UserRoleEntity, Long> {
    Flux<UserRoleEntity> findAllByUserId(Long userId);
    Flux<UserRoleEntity> findAllByRoleId(Integer roleId);
}
