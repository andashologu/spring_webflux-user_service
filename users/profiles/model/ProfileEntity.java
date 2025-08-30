package com.trademarket.api.security.users.profiles.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.trademarket.api.security.users.profiles.validation.UniqueUserId;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Table("profiles")
public class ProfileEntity {

    @Id
    private Long id;

    @NotNull(message = "User ID is required")
    @UniqueUserId //user can create multiple profiles
    private Long userId;

    @Size(max = 50, message = "Fist name must be at most 50 characters")
    @Pattern(regexp = "^[A-Za-z]+$", message = "First name must only contain letters")
    private String firstname;
    
    @Pattern(regexp = "^[A-Za-z]+$", message = "Last name must only contain letters")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    @Nullable
    private String lastname;

    @Column("profile_picture")
    @Size(max = 100, message = "Url must be at most 100 characters")
    private String profilePicture;
    
    @Size(max = 255, message = "Bio must not exceed 255 characters")
    private String bio; 
   
    @Pattern(
        regexp = "^(https?:\\/\\/)?([\\w\\-]+\\.)+[\\w\\-]+(/[\\w\\-./?%&=]*)?$", 
        message = "Website must be a valid URL"
    )
    @Size(max = 250, message = "Website must not exceed 250 characters")
    private String website;

    @Valid
    private Preferences preferences;

    @Valid
    private Settings settings;

    @Nonnull
    private Instant createdAt;

    private Instant updatedAt, accessedAt;

    public ProfileEntity() {}

    public ProfileEntity(Long id, Long userId, String firstname, String lastname, String profilePicture, String bio,
                         String website, Preferences preferences, 
                         Settings settings) {
        this.id = id;
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.website = website;
        this.preferences = preferences;
        this.settings = settings;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Preferences getPreferences() { return preferences; }
    public void setPreferences(Preferences preferences) { this.preferences = preferences; }

    public Settings getSettings() { return settings; }
    public void setSettings(Settings settings) { this.settings = settings; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getAccessedAt() { return accessedAt; }
    public void setAccessedAt(Instant accessedAt) { this.accessedAt = accessedAt; }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", userId=" + userId +
                ", firstname='" + firstname +
                ", lastname='" + lastname +
                ", profilePicture='" + profilePicture +
                ", bio='" + bio +
                ", website='" + website +
                ", preferences=" + preferences +
                ", settings=" + settings +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", accessedAt=" + accessedAt +
                '}';
    }
}
