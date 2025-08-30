package com.trademarket.api.security.users.authorities.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import com.trademarket.api.security.generics.RowMapper;
import com.trademarket.api.security.roles.model.RoleEntity;
import com.trademarket.api.security.roles.repository.RoleRepository;
import com.trademarket.api.security.users.authorities.model.UserRoleEntity;
import com.trademarket.api.security.users.authorities.repository.UserRoleRepository;
import com.trademarket.api.security.users.authorities.exception.RoleNotFoundException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserRoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    private final DatabaseClient databaseClient;

    RowMapper<UserRoleEntity> userRoleMapper = new RowMapper<>(UserRoleEntity.class);

    public UserRoleService( RoleRepository roleRepository, UserRoleRepository userRoleRepository, DatabaseClient databaseClient) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.databaseClient = databaseClient;
    }

    
    /*public Flux<UserRoleEntity> addRolesToAllUsers(List<Integer> roleIds) {//checked
        Instant now = Instant.now();
        // 1) Load and validate all requested RoleEntity objects once
        Flux<RoleEntity> validRoles = Flux.fromIterable(roleIds)
            .distinct()
            .flatMap(id -> roleRepository.findById(id)
                .onErrorResume(e -> {
                    System.out.println("Error loading Role id " + id + ": " + e.getMessage());
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    System.out.println("Skipping missing role id " + id);
                    return Mono.empty();
                }))
            )
            .cache(); // replay the same validated list for each user
    
        // 2) For each user, assign only those roles they don’t already have
        return validRoles
            .collectList()
            .flatMapMany(validList ->
                userRepository.findAll()
                    .flatMap(user ->
                        userRoleRepository.findAllByUserId(user.getId())
                            .map(UserRoleEntity::getRoleId)
                            .collect(Collectors.toSet())
                            .flatMapMany(existingIds ->
                                Flux.fromIterable(validList)
                                    .filter(role -> !existingIds.contains(role.getId()))
                                    .flatMap(role -> {
                                        UserRoleEntity ure = new UserRoleEntity(null, user.getId(), role.getId());
                                        ure.setUsername(user.getUsername());
                                        ure.setRoleName(role.getName());
                                        ure.setRoleDescription(role.getDescription());
                                        ure.setCreatedAt(now);
                                        ure.setAccessedAt(now);
                                        return userRoleRepository.save(ure)
                                            .onErrorResume(e -> {
                                                System.out.println("Failed to save UserRole for userId=" + user.getId() + ", roleId=" + role.getId() + ": " + e.getMessage());
                                                return Mono.empty();
                                            });
                                    })
                            )
                    )
            );
    }*/
    
    public Flux<UserRoleEntity> addRolesToSpecificUsers(Map<Long, Set<RoleEntity>> usersRolesMap) {
    Instant now = Instant.now();
    return Flux.fromIterable(usersRolesMap.entrySet())
        .flatMap(userRolesEntry -> {
            Long userId = userRolesEntry.getKey();
            Set<RoleEntity> userRolesSet = userRolesEntry.getValue();
            Flux<RoleEntity> verifiedUserRoles = Flux.fromIterable(userRolesSet)
                .flatMap(userRole -> {
                    if (userRole.getId() != null) {
                        Integer id = userRole.getId();
                        return roleRepository.findById(id)
                        .switchIfEmpty(Mono.error(new RoleNotFoundException("Role with id " + id + " not found")));
                            /* .switchIfEmpty(Mono.<RoleEntity>empty())
                            .onErrorResume(e -> {
                                System.err.println("Error loading Role by id " + id + ": " + e.getMessage());
                                return Mono.<RoleEntity>empty();
                            });*/
                    } else if (userRole.getName() != null) {
                        return roleRepository.findByName(userRole.getName(), now)
                            .switchIfEmpty(Mono.error(new RoleNotFoundException("Role with name " + userRole.getName() + " not found")));
                            /*.switchIfEmpty(Mono.<RoleEntity>empty())
                            .onErrorResume(e -> {
                                System.err.println("Error loading Role by name " + userRole.getName() + ": " + e.getMessage());
                                return Mono.<RoleEntity>empty();
                            });*/
                    } else return Mono.error(new RoleNotFoundException("Role not found"));
                })
                .distinct(RoleEntity::getId);
            return verifiedUserRoles
                .collectList()
                .flatMapMany(verifiedRolesList ->
                    userRoleRepository.findAllByUserId(userId)
                        .map(UserRoleEntity::getRoleId)
                        .collect(Collectors.toSet())
                        .flatMapMany(currentUserRolesIds ->
                            Flux.fromIterable(verifiedRolesList)
                                .filter(r -> !currentUserRolesIds.contains(r.getId()))
                                .flatMap(r -> {
                                    UserRoleEntity newUserRole = new UserRoleEntity(null, userId, r.getId());
                                    newUserRole.setRoleName(r.getName());
                                    newUserRole.setRoleDescription(r.getDescription());
                                    newUserRole.setCreatedAt(now);
                                    newUserRole.setAccessedAt(now);
                                    return userRoleRepository.save(newUserRole);
                                        /* .onErrorResume(e -> {
                                            System.err.println("Failed to save UserRole for userId=" +
                                                userId + ", roleId=" + r.getId() + ": " + e.getMessage());
                                            return Mono.<UserRoleEntity>empty();
                                        });*/
                                })
                        )
                );
        });
}


    public Flux<UserRoleEntity> getAllUserRoles(Long cursor, Integer limit) {
        // base query
        StringBuilder sb = new StringBuilder("""
            SELECT ur.id, ur.user_id, ur.username, ur.role_id, r.name AS role_name, r.description AS role_description, ur.created_at, ur.accessed_at
            FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            """);

        // optional cursor filter
        if (cursor != null) sb.append(" WHERE ur.id > :cursor");
        
        // ordering
        sb.append(" ORDER BY ur.id ASC");

        // optional limit
        if (limit != null) sb.append(" LIMIT :limit");
        
        // bind variables only if needed
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sb.toString());
        if (cursor != null) spec = spec.bind("cursor", cursor);
        if (limit != null) spec = spec.bind("limit", limit);
        
        return spec
            .map((row, _) -> userRoleMapper.map(row))
            .all();
    }

    public Flux<UserRoleEntity> getUserRoles(Long userId) {
        Instant now = Instant.now();
        String sql = """
    UPDATE user_roles ur
    SET accessed_at = :accessedAt
    FROM roles r, users u
    WHERE ur.role_id   = r.id
      AND ur.user_id   = u.id
      AND ur.user_id   = :userId
    RETURNING
      ur.id,
      ur.user_id,
      u.username           AS username,
      ur.role_id,
      r.name               AS role_name,
      r.description        AS role_description,
      ur.created_at,
      ur.accessed_at
    """;


        return databaseClient.sql(sql)
            .bind("accessedAt", now)
            .bind("userId", userId)
            .map((row, _) -> userRoleMapper.map(row))
            .all();
    }

    /*private UserRoleEntity mapRowToUserRoleEntity(Row row) {
        UserRoleEntity newUserRoleEntity = new UserRoleEntity();
        newUserRoleEntity.setId(row.get("id", Long.class));
        newUserRoleEntity.setUserId(row.get("user_id", Long.class));
        newUserRoleEntity.setUsername(row.get("username", String.class));
        newUserRoleEntity.setRoleId(row.get("role_id", Integer.class));
        newUserRoleEntity.setRoleName(row.get("role_name", String.class));
        newUserRoleEntity.setRoleDescription(row.get("role_description", String.class));
        newUserRoleEntity.setCreatedAt(row.get("created_at", Instant.class));
        newUserRoleEntity.setAccessedAt(row.get("accessed_at", Instant.class));
        return newUserRoleEntity;
    }*/
    
    public Mono<Void> removeUserRoles(List<Long> ids) {
        String sql = "DELETE FROM user_roles WHERE id = ANY(:ids)";
        return databaseClient.sql(sql)
            .bind("ids", ids)
            .fetch()
            .rowsUpdated()
            .then();
    }

    public Mono<String> removeUserRolesFromAllUsers(List<Integer> roleIds) {
        String sql = "DELETE FROM user_roles WHERE role_id = ANY(:roleIds)";
        return databaseClient.sql(sql)
            .bind("roleIds", roleIds)
            .fetch()
            .rowsUpdated()
            .thenReturn("Unassigned roles: " + roleIds);
    }

}
