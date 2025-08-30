package com.trademarket.api.security.users.authorities.model;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.annotation.Nonnull;

@Table("user_roles")
public class UserRoleEntity implements Serializable {

    @Id
    private Long id;
    
    private Long userId;

    @Transient
    private String username;
    
    private Integer roleId;

    @Transient
    private String roleName;

    @Transient
    private String roleDescription;

    @Nonnull
    private Instant createdAt;//added

    private Instant accessedAt;//added

    public UserRoleEntity() {}

    public UserRoleEntity(Long id, Long userId, Integer roleId) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
    }

    public UserRoleEntity(Long id, Long userId, String username, Integer roleId, String roleName, String roleDescription) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleDescription = roleDescription;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getRoleDescription() { return roleDescription; }
    public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getAccessedAt() { return accessedAt; }
    public void setAccessedAt(Instant accessedAt) { this.accessedAt = accessedAt; }
    
    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + id +
                ", userId=" + userId +
                ", username=" + username +
                ", roleId=" + roleId +
                ", roleName=" + roleName +
                ", roleDescription=" + roleDescription +
                ", createdAt=" + createdAt +
                ", accessedAt=" + accessedAt +
                "}";
    }
}
