package com.trademarket.api.security.users.controller;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.trademarket.api.exceptions.ValidationException;
import com.trademarket.api.security.config.model.CustomUserDetails;
import com.trademarket.api.security.roles.model.PermissionEntity;
import com.trademarket.api.security.roles.model.RoleEntity;
import com.trademarket.api.security.users.authorities.exception.PermissionNotFoundException;
import com.trademarket.api.security.users.authorities.exception.RoleNotFoundException;
import com.trademarket.api.security.users.authorities.service.UserPermissionService;
import com.trademarket.api.security.users.authorities.service.UserRoleService;
import com.trademarket.api.security.users.exception.UserNotFoundException;
import com.trademarket.api.security.users.model.UserEntity;
import com.trademarket.api.security.users.repository.CustomUserRepository;
import com.trademarket.api.security.users.repository.UserRepository;
import com.trademarket.api.security.users.service.UserService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserRepository customUserRepository;

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final UserPermissionService userPermissionService;

    public UserController(UserService userService, UserRoleService userRoleService, UserPermissionService userPermissionService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.userPermissionService = userPermissionService;
    }

    // CREATE.............................................................................................
    
    @PostMapping
    public Mono<UserEntity> createUser(@RequestBody UserEntity userEntity) {
        Instant now = Instant.now();
        userEntity.setCreatedAt(now);
        userEntity.setUpdatedAt(now);
        userEntity.setAccessedAt(now);
        return userService.saveUser(userEntity)
            // 1) assign roles
            .flatMap(savedUserEntity -> {
                Map<Long, Set<RoleEntity>> rolesMap = Map.of(savedUserEntity.getId(), new HashSet<>(savedUserEntity.getRoles()));
                return userRoleService.addRolesToSpecificUsers(rolesMap)
                    .collectList()
                    .thenReturn(savedUserEntity);
            })
            // 2) assign permissions
            .flatMap(savedUserEntity -> {
                Map<Long, Set<PermissionEntity>> permissionsMap = Map.of(savedUserEntity.getId(), new HashSet<>(savedUserEntity.getPermissions()));
                return userPermissionService.addPermissionsToSpecificUsers(permissionsMap)
                    .collectList()
                    .thenReturn(savedUserEntity);
            })
            .onErrorMap(UserNotFoundException.class,
                ex -> new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex))
            .onErrorMap(RoleNotFoundException.class,
                ex -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex))
            .onErrorMap(PermissionNotFoundException.class,
                ex -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex))
            .onErrorMap(ValidationException.class,
                ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getErrors().toString(), ex));
    }
    
    //READ....................................................................................................................
    
    @GetMapping("/{id:\\d+}")
    public Mono<UserEntity> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id "+ id + " not found")));
    }

    @GetMapping("/{username:^(?!\\d+$)[A-Za-z0-9][A-Za-z0-9_]{0,29}$}")
    public Mono<UserEntity> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with username " + username+ " not found")));
    }

    @GetMapping
    public Flux<UserEntity> getAllUsers(
        @RequestParam(required = false) Long cursor,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String direction,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String country,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String street) {
        return customUserRepository.findAllPaginated(cursor, size, sortBy, direction, search, country, city, region, street);
    }
    
    @GetMapping("/current_user")
    @PreAuthorize("isAuthenticated()")
    public Mono<CustomUserDetails> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getPrincipal)
            .cast(CustomUserDetails.class);
    }

    //UPDATE...............................................................................................................    @PatchMapping("/{id}")
    public Mono<Object> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        if (updates.containsKey("password")) updates.remove("password");
        return userService.updateUser(id, updates)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .map(updatedUser -> (Object) updatedUser)
            .onErrorMap(UserNotFoundException.class,
                ex -> new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex))
            .onErrorMap(ValidationException.class,
                ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getErrors().toString(), ex));
    }

    //DELETE...............................................................................................................    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return userRepository.deleteById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .onErrorResume(ex -> Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred", ex)));
    }
    
    @DeleteMapping("/current_user")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(context -> {
                CustomUserDetails customUserDetails = (CustomUserDetails) context.getAuthentication().getPrincipal();
                return userRepository.deleteById(customUserDetails.getId());
            });
    }
} 
