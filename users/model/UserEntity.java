package com.trademarket.api.security.users.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.trademarket.api.security.roles.model.PermissionEntity;
import com.trademarket.api.security.roles.model.RoleEntity;
import com.trademarket.api.security.users.addresses.model.AddressEntity;
import com.trademarket.api.security.users.profiles.model.ProfileEntity;
import com.trademarket.api.security.users.validation.UniqueEmail;
import com.trademarket.api.security.users.validation.UniqueMobileNumber;
import com.trademarket.api.security.users.validation.UniqueUsername;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Table("users")
public class UserEntity implements Serializable {
    @Id
    private Long id;

    @Pattern(
      regexp = "^(?!\\d+$)[A-Za-z0-9][A-Za-z0-9_]{0,29}$",
      message = "Username must be 1-30 chars, start with letter/number, may include underscore, not all digits"
    )
    @UniqueUsername
    private String username;

    @Nullable
    @Email(message = "Email must be a valid format")
    @UniqueEmail
    @Size(max = 254, message = "Email must be at most 254 characters")
    private String email;

    @JsonProperty(access = Access.READ_ONLY) //for security purposes, frontend must not write
    private Boolean emailVerified = false;

    @Pattern(
        regexp = "^[1-9]\\d{7,14}$",
        message = "Mobile number must be 8–15 digits and contain only numbers, and must not start with 0"
    ) //Frontend must make sure 0 is omitted at the beginning
    @UniqueMobileNumber
    private String mobileNumber;

    @JsonProperty(access = Access.READ_ONLY) //for security purposes, frontend must not write
    private Boolean mobileNumberVerified = false;

    @Pattern(regexp = "^\\+[1-9]\\d{1,3}$", message = "Invalid country code format")
    private String countryCode;

    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,64}$",
        message = "Password must be 8-64 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&#)."
    )
    @JsonProperty(access = Access.WRITE_ONLY) //for security purposes, exclude from response
    private String password;

    @Transient //for database
    //@JsonIgnore //for frontend
    //@JsonDeserialize(using = RoleMapDeserializer.class)
    private List<RoleEntity> roles;

    @Transient
    private List<PermissionEntity> permissions;

    @Nonnull
    @JsonProperty(access = Access.READ_ONLY) //for security purposes, frontend must not write
    private Boolean active = false;

    private Boolean accountNonExpired = true;

    @Nonnull
    private Boolean accountNonLocked = true;

    @Nonnull
    private Boolean credentialsNonExpired = true;

    @Nonnull
    private Instant createdAt;

    private Instant updatedAt, accessedAt;

    private Instant expiryDate; //measure to given period calculated from accessedAt

    @Transient
    private ProfileEntity profileEntity;

    @Transient 
    private AddressEntity addressEntity;
    public UserEntity(){}

    public UserEntity(Long id, String username, String email, String mobileNumber, String countryCode, String password, List<RoleEntity> roles, List<PermissionEntity> permissions, ProfileEntity profileEntity, AddressEntity addressEntity) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.countryCode = countryCode;
        this.password = password;
        this.roles = roles; 
        this.permissions = permissions;
        this.profileEntity = profileEntity;
        this.addressEntity = addressEntity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public Boolean isMobileNumberVerified() { return mobileNumberVerified; }
    public void setMobileNumberVerified(Boolean mobileNumberVerified) { this.mobileNumberVerified = mobileNumberVerified; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    @AssertTrue(message = "Provide both country code and mobile number together, or omit both if email is provided")
    public boolean isContactInfoValid() {
        boolean hasMobile = mobileNumber != null && !mobileNumber.isBlank();
        boolean hasCountryCode = countryCode != null && !countryCode.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        return (hasMobile && hasCountryCode) || ((!hasMobile && !hasCountryCode) && hasEmail);
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean isActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getAccountNonExpired() { return accountNonExpired; }
    public void setAccountNonExpired(Boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }

    public Boolean getAccountNonLocked() { return accountNonLocked; }
    public void setAccountNonLocked(Boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }

    public Boolean getCredentialsNonExpired() { return credentialsNonExpired; }
    public void setCredentialsNonExpired(Boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }

    public List<RoleEntity> getRoles() { return roles; }
    public void setRoles(List<RoleEntity> roles) { this.roles = roles; }

    public List<PermissionEntity> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionEntity> permissions) { this.permissions = permissions; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getAccessedAt() { return accessedAt; }
    public void setAccessedAt(Instant accessedAt) { this.accessedAt = accessedAt; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public ProfileEntity getProfileEntity(){ return profileEntity; }
    public void setProfileEntity(ProfileEntity profileEntity){ this.profileEntity = profileEntity; }

    public AddressEntity getAddressEntity(){ return addressEntity; }
    public void setAddressEntity(AddressEntity addressEntity){ this.addressEntity = addressEntity; }

    @Override
    public String toString() {
        return "UserEntity{" +
            "id=" + id +
            ", username=" + username +
            ", email=" + email +
            ", emailVerified=" + emailVerified +
            ", mobileNumber=" + mobileNumber +
            ", mobileNumberVerified=" +mobileNumberVerified +
            ", countryCode=" + countryCode +
            ", roles='" + roles +
            ", permissions='" + permissions +
            ", active=" + active +
            ", accountNonExpired=" + accountNonExpired +
            ", accountNonLocked=" + accountNonLocked +
            ", credentialsNonExpired=" + credentialsNonExpired +
            ", roles=" + roles +
            ", permissions=" + permissions +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            ", accessedAt=" + accessedAt +
            ", expiryDate=" + expiryDate +
            ", profileEntity=" + profileEntity +
            ", addressEntity=" + addressEntity + 
            '}';
    }
}

