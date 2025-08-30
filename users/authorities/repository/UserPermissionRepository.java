package com.trademarket.api.security.users.authorities.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.trademarket.api.security.users.authorities.model.UserPermissionEntity;

import reactor.core.publisher.Flux;

@Repository
public interface UserPermissionRepository extends R2dbcRepository<UserPermissionEntity, Long> {
    Flux<UserPermissionEntity> findAllByUserId(Long userId);
    Flux<UserPermissionEntity> findAllByPermissionId(Integer roleId);
}
