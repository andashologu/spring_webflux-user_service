package com.trademarket.api.security.users.authorities.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import com.trademarket.api.security.generics.RowMapper;
import com.trademarket.api.security.roles.model.PermissionEntity;
import com.trademarket.api.security.roles.repository.PermissionRepository;
import com.trademarket.api.security.users.authorities.model.UserPermissionEntity;
import com.trademarket.api.security.users.authorities.repository.UserPermissionRepository;
import com.trademarket.api.security.users.repository.UserRepository;
import com.trademarket.api.security.users.authorities.exception.PermissionNotFoundException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserPermissionService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final DatabaseClient databaseClient;

    RowMapper<UserPermissionEntity> userPermissionMapper = new RowMapper<>(UserPermissionEntity.class);

    public UserPermissionService(UserRepository userRepository, PermissionRepository permissionRepository, UserPermissionRepository userPermissionRepository, DatabaseClient databaseClient) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.databaseClient = databaseClient;
    }

    public Flux<UserPermissionEntity> addPermissionsToSpecificUsers(Map<Long, Set<PermissionEntity>> usersPermsMap) {
        Instant now = Instant.now();
        return Flux.fromIterable(usersPermsMap.entrySet())
            .flatMap(entry -> {
                Long userId = entry.getKey();
                Set<PermissionEntity> permsSet = entry.getValue();
                Flux<PermissionEntity> verifiedPerms = Flux.fromIterable(permsSet)
                    .flatMap(permRef -> {
                        if (permRef.getId() != null) {
                            Integer id = permRef.getId();
                            return permissionRepository.findById(id)
                                .switchIfEmpty(Mono.error(new PermissionNotFoundException("Permission with id " + id + " not found")));
                                /*.switchIfEmpty(Mono.<PermissionEntity>empty())
                                .onErrorResume(e -> {
                                    System.err.println("Error loading Permission by id " + id + ": " + e.getMessage());
                                    return Mono.<PermissionEntity>empty();
                                });*/
                        } else if (permRef.getName() != null) {
                            return permissionRepository.findByName(permRef.getName(), now)
                                .switchIfEmpty(Mono.error(new PermissionNotFoundException("Permission with name " + permRef.getName() + " not found")));
                                /*.switchIfEmpty(Mono.<PermissionEntity>empty())
                                .onErrorResume(e -> {
                                    System.err.println("Error loading Permission by name '" + permRef.getName() + "': " + e.getMessage());
                                    return Mono.<PermissionEntity>empty();
                                });*/
                        } else return Mono.error(new PermissionNotFoundException("Permission not found"));
                    }).distinct(PermissionEntity::getId);
                return verifiedPerms
                    .collectList()
                    .flatMapMany(validList ->
                        userPermissionRepository.findAllByUserId(userId)
                            .map(UserPermissionEntity::getPermissionId)
                            .collect(Collectors.toSet())
                            .flatMapMany(existingIds ->
                                Flux.fromIterable(validList)
                                    .filter(p -> !existingIds.contains(p.getId()))
                                    .flatMap(p -> {
                                        UserPermissionEntity upe = new UserPermissionEntity(null, userId, p.getId());
                                        upe.setPermissionName(p.getName());
                                        upe.setPermissionDescription(p.getDescription());
                                        upe.setCreatedAt(now);
                                        upe.setAccessedAt(now);
                                        return userPermissionRepository.save(upe)
                                            .onErrorResume(e -> {
                                                System.err.println("Failed to save UserPermission for userId="
                                                    + userId + ", permissionId=" + p.getId() + ": " + e.getMessage());
                                                return Mono.empty();
                                            });
                                    })
                            )
                    );
            });
    }

    public Flux<UserPermissionEntity> addPermissionsToAllUsers(List<Integer> permissionIds) {
        Instant now = Instant.now();

        Flux<PermissionEntity> validPerms = Flux.fromIterable(permissionIds)
            .distinct()
            .flatMap(id ->
                permissionRepository.findById(id)
                    // ↑ When missing, return an empty Mono<PermissionEntity>
                    .switchIfEmpty(Mono.<PermissionEntity>empty())
                    // ↑ On error, log and return empty Mono<PermissionEntity>
                    .onErrorResume(e -> {
                        System.err.println("Error loading Permission id " + id + ": " + e.getMessage());
                        return Mono.<PermissionEntity>empty();
                    })
            )
            .cache();

        return validPerms
            .collectList()
            .flatMapMany(list ->
                userRepository.findAll()
                    .flatMap(user ->
                        userPermissionRepository.findAllByUserId(user.getId())
                            .map(UserPermissionEntity::getPermissionId)
                            .collect(Collectors.toSet())
                            .flatMapMany(existingIds ->
                                Flux.fromIterable(list)
                                    .filter(p -> !existingIds.contains(p.getId()))
                                    .flatMap(p -> {
                                        UserPermissionEntity upe = new UserPermissionEntity(null, user.getId(), p.getId());
                                        upe.setUsername(user.getUsername());
                                        upe.setPermissionName(p.getName());
                                        upe.setPermissionDescription(p.getDescription());
                                        upe.setCreatedAt(now);
                                        upe.setAccessedAt(now);
                                        return userPermissionRepository.save(upe)
                                            // ↑ On save error, return empty Mono<UserPermissionEntity>
                                            .onErrorResume(e -> {
                                                System.err.println(
                                                "Failed to save UserPermission for userId=" +
                                                user.getId() +
                                                ", permissionId=" + p.getId() +
                                                ": " + e.getMessage()
                                                );
                                                return Mono.<UserPermissionEntity>empty();
                                            });
                                    })
                            )
                    )
            );
    }

    public Flux<UserPermissionEntity> getAllUserPermissions(Long cursor, Integer limit) {
        StringBuilder sb = new StringBuilder("""
            SELECT
              up.id, up.user_id, up.username,
              up.permission_id, p.name        AS permission_name,
                              p.description AS permission_description,
              up.created_at, up.accessed_at
            FROM user_permissions up
            JOIN permissions p ON up.permission_id = p.id
            """);
        if (cursor != null) sb.append(" WHERE up.id > :cursor");
        
        sb.append(" ORDER BY up.id ASC");
        if (limit != null) sb.append(" LIMIT :limit");
    
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sb.toString());
        if (cursor != null) spec = spec.bind("cursor", cursor);
        if (limit != null)  spec = spec.bind("limit",  limit);
    
        return spec
            .map((row, _) -> userPermissionMapper.map(row))
            .all();
    }

    public Flux<UserPermissionEntity> getUserPermissions(Long userId) {
        Instant now = Instant.now();
        String sql = """
    UPDATE user_permissions up
    SET accessed_at = :now
    FROM permissions p, users u
    WHERE up.permission_id = p.id
      AND up.user_id       = u.id
      AND up.user_id       = :userId
    RETURNING
      up.id,
      up.user_id,
      u.username            AS username,
      up.permission_id,
      p.name                AS permission_name,
      p.description         AS permission_description,
      up.created_at,
      up.accessed_at
    """;

        return databaseClient.sql(sql)
            .bind("now", now)
            .bind("userId", userId)
            .map((row, _) -> userPermissionMapper.map(row))
            .all();
    }

    /*private UserPermissionEntity mapRowToUserPermissionEntity(Row row) {
        UserPermissionEntity upe = new UserPermissionEntity();
        upe.setId(row.get("id", Long.class));
        upe.setUserId(row.get("user_id", Long.class));
        upe.setUsername(row.get("username", String.class));
        upe.setPermissionId(row.get("permission_id", Integer.class));
        upe.setPermissionName(row.get("permission_name", String.class));
        upe.setPermissionDescription(row.get("permission_description", String.class));
        upe.setCreatedAt(row.get("created_at", Instant.class));
        upe.setAccessedAt(row.get("accessed_at", Instant.class));
        return upe;
    }*/

    public Mono<Void> removeUserPermissions(List<Long> ids) {
        String sql = "DELETE FROM user_permissions WHERE id = ANY(:ids)";
        return databaseClient.sql(sql)
            .bind("ids", ids)
            .fetch()
            .rowsUpdated()
            .then();
    }
    
    public Mono<String> removeUserPermissionsFromAllUsers(List<Integer> permissionIds) {
        String sql = "DELETE FROM user_permissions WHERE permission_id = ANY(:permissionIds)";
        return databaseClient.sql(sql)
            .bind("permissionIds", permissionIds)
            .fetch()
            .rowsUpdated()
            .thenReturn("Unassigned permissions: " + permissionIds);
    }
    
}
