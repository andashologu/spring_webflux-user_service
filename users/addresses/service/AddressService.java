package com.trademarket.api.security.users.addresses.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import com.trademarket.api.security.generics.CustomRepository;
import com.trademarket.api.security.generics.CustomValidation;
import com.trademarket.api.security.generics.RowMapper;
import com.trademarket.api.security.users.addresses.model.AddressEntity;
import com.trademarket.api.security.users.addresses.repository.AddressRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomValidation<AddressEntity> customValidation;
    private final CustomRepository<AddressEntity, Long> customRepository;
    private final DatabaseClient databaseClient;

    RowMapper<AddressEntity> addressMapper = new RowMapper<>(AddressEntity.class);

    public AddressService(AddressRepository addressRepository, CustomValidation<AddressEntity> customValidation, CustomRepository<AddressEntity, Long> customRepository, DatabaseClient databaseClient) {
        this.addressRepository = addressRepository;
        this.customValidation = customValidation;
        this.databaseClient = databaseClient;
        this.customRepository = customRepository;
    }

    public Mono<AddressEntity> saveAddress(AddressEntity address) {
        return Mono.fromCallable(() -> {
                    System.out.println("AddressService: Validating address fields");
                    customValidation.validateAll(address);
                    return address;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(validatedAddress -> {
                    System.out.println("AddressService: Persisting validated address to the database");
                    return addressRepository.save(validatedAddress)
                    .doOnNext(savedAddress -> System.out.println("AddressService: Address saved with ID: " + savedAddress.getId()));
                });
    }

    public Flux<AddressEntity> getAll(Integer cursor, Integer limit, String country, String city, String region, String street) {
        StringBuilder sql = new StringBuilder("SELECT * FROM addresses");

        boolean hasWhere = false;
        if (country != null && !country.isBlank()) {
            sql.append(" WHERE country = :country");
            hasWhere = true;
        }
        if (city != null && !city.isBlank()) {
            sql.append(hasWhere ? " AND" : " WHERE")
               .append(" city = :city");
            hasWhere = true;
        }
        if (region != null && !region.isBlank()) {
            sql.append(hasWhere ? " AND" : " WHERE")
               .append(" region = :region");
            hasWhere = true;
        }
        if (street != null && !street.isBlank()) {
            sql.append(hasWhere ? " AND" : " WHERE")
               .append(" street = :street");
            hasWhere = true;
        }
        if (cursor != null) {
            sql.append(hasWhere ? " AND" : " WHERE")
               .append(" id > :cursor");
        }

        sql.append(" ORDER BY id");
        if (limit != null) {
            sql.append(" LIMIT :limit");
        }

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (country != null && !country.isBlank()) spec = spec.bind("country", country);
        if (city    != null && !city.isBlank()) spec = spec.bind("city", city);
        if (region  != null && !region.isBlank()) spec = spec.bind("region", region);
        if (street  != null && !street.isBlank()) spec = spec.bind("street", street);
        if (cursor  != null) spec = spec.bind("cursor", cursor);
        if (limit   != null) spec = spec.bind("limit", limit);

        return spec
            .map((row, _) -> addressMapper.map(row))
            .all();
    }

    /*private AddressEntity mapRowToAddressEntity(Row row) {
        AddressEntity a = new AddressEntity();
        a.setId(row.get("id", Long.class));
        a.setUserId(row.get("user_id", Long.class));
        a.setCountry(row.get("country", String.class));
        a.setRegion(row.get("region", String.class));
        a.setCity(row.get("city", String.class));
        a.setStreet(row.get("street", String.class));
        a.setUnitNumber(row.get("unit_number", String.class));
        a.setZipCode(row.get("zip_code", String.class));
        a.setType(row.get("type", String.class));
        a.setAdditionalInfo(row.get("additionalInfo", String.class));
        a.setLatitude(row.get("latitude", Double.class));
        a.setLongitude(row.get("longitude", Double.class));
        a.setCreatedAt(row.get("created_at", Instant.class));
        a.setUpdatedAt(row.get("updated_at", Instant.class));
        a.setAccessedAt(row.get("accessed_at", Instant.class));
        return a;
    }*/
    
    public Mono<Object> updateAddress(AddressEntity addressEntity, Map<String, Object> updates) {
        updates.put("updatedAt", Instant.now());
        updates.put("accessedAt", Instant.now());
        return Mono.fromCallable(() -> {
                customValidation.validate(addressEntity, updates);
                return addressEntity;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(validatedAddress -> {
                Long id = validatedAddress.getId(); 
                if (id == null) return Mono.error(new IllegalArgumentException("Address ID cannot be null for update"));
                return customRepository.updateFields(id, updates, AddressEntity.class, validatedAddress);
            });
    }
    
}
