package com.trademarket.api.security.users.addresses.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Table("addresses")
public class AddressEntity {

    @Id
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @Size(max = 255, message = "Country must not exceed 255 characters")
    private String country;

    @Size(max = 255, message = "Region must not exceed 255 characters")
    private String region;

    @Size(max = 255, message = "City must not exceed 255 characters")
    private String city;

    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;

    @Size(max = 50, message = "Unit number must not exceed 50 characters")
    private String unitNumber;

    @Size(max = 50, message = "Zip code must not exceed 50 characters")
    private String zipCode;

    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    private String additionalInfo;

    private Double latitude;
    private Double longitude;

    @Nonnull
    private Instant createdAt;

    private Instant updatedAt;
    private Instant accessedAt;

    public AddressEntity() {}

    public AddressEntity(Long id, Long userId, String country, String city, String region, String street, String unitNumber, String zipCode, String type, String additionalInfo, Double latitude, Double longitude) {
        this.id = id;
        this.userId = userId;
        this.country = country;
        this.city = city;
        this.region = region;
        this.street = street;
        this.unitNumber = unitNumber;
        this.zipCode = zipCode;
        this.type = type;
        this.additionalInfo = additionalInfo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalinfo) { this.additionalInfo = additionalinfo; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getAccessedAt() { return accessedAt; }
    public void setAccessedAt(Instant accessedAt) { this.accessedAt = accessedAt; }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", userId=" + userId +
                ", country=" + country +
                ", region=" + region +
                ", city=" + city +
                ", street=" + street +
                ", unitNumber=" + unitNumber +
                ", zipCode=" + zipCode +
                ", type=" + type +
                ", additionalInfo=" + additionalInfo +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", accessedAt=" + accessedAt +
                '}';
    }
}
