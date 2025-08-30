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

import com.trademarket.api.security.roles.model.RoleEntity;
import com.trademarket.api.security.users.authorities.model.UserRoleEntity;
import com.trademarket.api.security.users.authorities.service.UserRoleService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users/roles")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @PostMapping("/assign_roles")
    public Flux<UserRoleEntity> assignToUsers( @RequestBody Map<Long, Set<RoleEntity>> usersRolesMap) {//checked
        return userRoleService.addRolesToSpecificUsers(usersRolesMap);
    }

    /*@PostMapping("/assign_all")
    public Flux<UserRoleEntity> assignToAll(@RequestBody List<Integer> roleIds) {
        return userRoleService.addRolesToAllUsers(roleIds);
    }*/

    @GetMapping
    public Flux<UserRoleEntity> getAllAssignments(
        @RequestParam(value = "cursor", required = false) Long cursor,
        @RequestParam(value = "limit",  required = false) Integer limit
    ) {
        return userRoleService.getAllUserRoles(cursor, limit);
    }

    @GetMapping(params="userId")
    public Flux<UserRoleEntity> getByUser(@RequestParam Long userId) {
        return userRoleService.getUserRoles(userId);
    }

    @DeleteMapping("/unassign")
    public Mono<ResponseEntity<Void>> unassign(@RequestBody List<Long> userRoleIds) {
        return userRoleService.removeUserRoles(userRoleIds)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @DeleteMapping("/unassign_all")
    public Mono<ResponseEntity<String>> unassignAll(@RequestBody List<Integer> roleIds) {
        return userRoleService.removeUserRolesFromAllUsers(roleIds)
                .map(ResponseEntity::ok);
    }
}
