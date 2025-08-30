package com.trademarket.api.security.users.authorities.model;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.annotation.Nonnull;

@Table("user_permissions")
public class UserPermissionEntity implements Serializable {
    
    @Id
    private Long id;

    private Long userId;

    @Transient
    private String username;

    private Integer permissionId;

    @Transient
    private String permissionName;

    @Transient
    private String permissionDescription;

    @Nonnull
    private Instant createdAt;//added

    private Instant accessedAt;//added

    public UserPermissionEntity() {}

    public UserPermissionEntity(Long id, Long userId, Integer permissionId) {
        this.id = id;
        this.userId = userId;
        this.permissionId = permissionId;

    }

    public UserPermissionEntity(Long id, Long userId, String username, Integer permissionId, String permissionName, String permissionDescription) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.permissionDescription =permissionDescription;

    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer userPermissionId) { this.permissionId = userPermissionId; }

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }

    public String getPermissionDescription() { return permissionDescription; }
    public void setPermissionDescription(String permissionDescription) { this.permissionDescription = permissionDescription; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getAccessedAt() { return accessedAt; }
    public void setAccessedAt(Instant accessedAt) { this.accessedAt = accessedAt; }
    
    @Override
    public String toString() {
        return "UserPermissions{" +
                "id=" + id +
                ", username=" + username +
                "userId=" + userId +
                ", permissionId=" + permissionId +
                ", permissionName=" + permissionName +
                ", permissionDescription=" + permissionDescription +
                ", createdAt=" + createdAt +
                ", accessedAt=" + accessedAt +
                "}";
    }
}
