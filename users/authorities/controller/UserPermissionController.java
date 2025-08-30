// src/main/java/com/trademarket/tzm/users/authorities/controller/UserPermissionController.java
package com.trademarket.api.security.users.authorities.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trademarket.api.security.roles.model.PermissionEntity;
import com.trademarket.api.security.users.authorities.model.UserPermissionEntity;
import com.trademarket.api.security.users.authorities.service.UserPermissionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users/permissions")
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    public UserPermissionController(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    /** ASSIGN specific users → many at once */
    @PostMapping("/assign_permissions")
    public Flux<UserPermissionEntity> assignToUsers(@RequestBody Map<Long, Set<PermissionEntity>> userPermissionsMap) {
        return userPermissionService.addPermissionsToSpecificUsers(userPermissionsMap);
    }

    /** ASSIGN to all users → list of perm‑IDs */
    @PostMapping("/assign_all")
    public Flux<UserPermissionEntity> assignToAll(@RequestBody List<Integer> permissionIds) {
        return userPermissionService.addPermissionsToAllUsers(permissionIds);
    }

    /** READ all assignments */
    @GetMapping("/permissions")
    public Flux<UserPermissionEntity> getAllAssignments(
        @RequestParam(value = "cursor", required = false) Long cursor,
        @RequestParam(value = "limit",  required = false) Integer limit
    ) {
        return userPermissionService.getAllUserPermissions(cursor, limit);
    }

    /** READ by user */
    @GetMapping(params="userId")
    public Flux<UserPermissionEntity> getByUser(@RequestParam Long userId) {
        return userPermissionService.getUserPermissions(userId);
    }

    /** UNASSIGN specific assignment IDs */
    @DeleteMapping("/unassign")
    public Mono<ResponseEntity<Void>> unassign(
            @RequestBody List<Long> userPermissionIds) {
        return userPermissionService.removeUserPermissions(userPermissionIds)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    /** UNASSIGN from all users (by perm‑IDs) */
    @DeleteMapping("/unassign_all")
    public Mono<ResponseEntity<String>> unassignAll(
            @RequestBody List<Integer> permissionIds) {
        return userPermissionService.removeUserPermissionsFromAllUsers(permissionIds)
                .map(ResponseEntity::ok);
    }
}
