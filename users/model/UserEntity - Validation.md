```java
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

    @Pattern(
        regexp = "^[1-9]\\d{7,14}$",
        message = "Mobile number must be 8–15 digits and contain only numbers, and must not start with 0"
    )
    @UniqueMobileNumber
    private String mobileNumber;

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
    private List<RoleEntity> roles;

    @Transient
    private List<PermissionEntity> permissions;

    @Nonnull
    private Boolean active = false;

    private Boolean accountNonExpired = true;

    @Nonnull
    private Boolean accountNonLocked = true;

    @Nonnull
    private Boolean credentialsNonExpired = true;

    @Nonnull
    private Instant createdAt;

    @Transient
    private ProfileEntity profileEntity;
```