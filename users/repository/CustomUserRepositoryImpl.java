package com.trademarket.api.security.users.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademarket.api.security.generics.RowMapper;
import com.trademarket.api.security.users.addresses.model.AddressEntity;
import com.trademarket.api.security.users.model.UserEntity;
import com.trademarket.api.security.users.profiles.model.ProfileEntity;

import reactor.core.publisher.Flux;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository{

    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;

    RowMapper<UserEntity> userMapper = new RowMapper<>(UserEntity.class);
    RowMapper<ProfileEntity> profileMapper = new RowMapper<>(ProfileEntity.class);
    RowMapper<AddressEntity> addressMapper = new RowMapper<>(AddressEntity.class);

    public CustomUserRepositoryImpl(DatabaseClient databaseClient, ObjectMapper objectMapper) {
        this.databaseClient = databaseClient;
        this.objectMapper = objectMapper;
    }

    /* DO NOT DELETE
     instead of "COALESCE(json_agg(DISTINCT r) FILTER (WHERE r.id IS NOT NULL), '[]') AS roles"
     use "
        COALESCE(
            jsonb_agg(
                DISTINCT jsonb_build_object(
                    'id',           r.id,
                    'name',         r.name,
                    'description',  r.description,
                    'createdAt',    r.created_at,
                    'updatedAt',    r.updated_at,
                    'accessedAt',   r.accessed_at
                )
            ) FILTER (WHERE r.id IS NOT NULL),
            '[]'::jsonb
        ) AS roles
     "
     because json is not set for camel case
     */
    @Override
    public Flux<UserEntity> findAllPaginated(Long cursor, Integer size, String sortBy, String direction, String search, String country, String city, String region, String street) {

        StringBuilder sql = new StringBuilder("""
            SELECT
            users.*,
            profiles.*,
            addresses.*,
            COALESCE(
                jsonb_agg(DISTINCT jsonb_build_object(
                'id',           r.id,
                'name',         r.name,
                'description',  r.description,
                'createdAt',    r.created_at,
                'updatedAt',    r.updated_at,
                'accessedAt',   r.accessed_at
                )) FILTER (WHERE r.id IS NOT NULL),
                '[]'::jsonb
            ) AS roles,
            COALESCE(
                jsonb_agg(DISTINCT jsonb_build_object(
                'id',           p.id,
                'name',         p.name,
                'description',  p.description,
                'createdAt',    p.created_at,
                'updatedAt',    p.updated_at,
                'accessedAt',   p.accessed_at
                )) FILTER (WHERE p.id IS NOT NULL),
                '[]'::jsonb
            ) AS permissions
            FROM users
            LEFT JOIN profiles         ON users.id = profiles.user_id
            LEFT JOIN addresses        ON users.id = addresses.user_id
            LEFT JOIN user_roles       ur   ON users.id = ur.user_id
            LEFT JOIN roles            r    ON ur.role_id = r.id
            LEFT JOIN user_permissions up   ON users.id = up.user_id
            LEFT JOIN permissions      p    ON up.permission_id = p.id
            """);

        boolean hasWhere = false;

        // --- WHERE clauses ---
        if (cursor != null) {
            sql.append(" WHERE users.id > :cursor ");
            hasWhere = true;
        }
        if (search != null && !search.isBlank()) {
            sql.append(hasWhere ? " AND " : " WHERE ")
                .append("""
                (
                    to_tsvector('simple', coalesce(users.username,'')) 
                    @@ to_tsquery('simple', :searchQuery || ':*')
                    OR
                    to_tsvector('simple',
                    coalesce(profiles.firstname,'') || ' ' || coalesce(profiles.lastname,'')
                    ) @@ to_tsquery('simple', :searchQuery || ':*')
                )
                """);
            hasWhere = true;
        }
        if (country != null && !country.isBlank()) {
            sql.append(hasWhere ? " AND " : " WHERE ")
                .append(" country ILIKE '%'||TRIM(:country)||'%' ");
            hasWhere = true;
        }
        if (city != null && !city.isBlank()) {
            sql.append(hasWhere ? " AND " : " WHERE ")
                .append(" city ILIKE '%'||TRIM(:city)||'%' ");
            hasWhere = true;
        }
        if (region != null && !region.isBlank()) {
            sql.append(hasWhere ? " AND " : " WHERE ")
                .append(" region ILIKE '%'||TRIM(:region)||'%' ");
            hasWhere = true;
        }
        if (street != null && !street.isBlank()) {
            sql.append(hasWhere ? " AND " : " WHERE ")
                .append(" street ILIKE '%'||TRIM(:street)||'%' ");
        }

        // --- GROUP BY (must come before ORDER BY / LIMIT) ---
        sql.append(" GROUP BY users.id, profiles.id, addresses.id ");

        // --- ORDER BY ---
        if (sortBy != null && !sortBy.isBlank()) {
            String safeSortBy = switch (sortBy) {
                case "id", "created_at", "username", "email" -> "users." + sortBy;
                case "firstname" -> "profiles.firstname";
                case "lastname" -> "profiles.lastname";
                case "fullname" -> "(profiles.firstname || ' ' || profiles.lastname)";
                default -> "users.id";
            };
            String safeDir = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";
            sql.append(" ORDER BY ").append(safeSortBy).append(" ").append(safeDir).append(" ");
        }

        // --- LIMIT ---
        if (size != null) {
            sql.append(" LIMIT :limit ");
        }

        // **NOW** create the spec _after_ you have the final SQL string
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());

        // bind parameters
        if (cursor != null) spec = spec.bind("cursor", cursor);
        if (search != null && !search.isBlank()) spec = spec.bind("searchQuery", search);
        if (country != null && !country.isBlank()) spec = spec.bind("country", country);
        if (city != null && !city.isBlank()) spec = spec.bind("city", city);
        if (region != null && !region.isBlank()) spec = spec.bind("region", region);
        if (street != null && !street.isBlank()) spec = spec.bind("street", street);
        if (size != null) spec = spec.bind("limit", size);

                return spec.map((row, _) -> {
                    UserEntity user = userMapper.map(row);

                    // Manually map transient fields (roles, permissions, etc.)
                    String rolesJson = row.get("roles", String.class);
                    String permsJson = row.get("permissions", String.class);

                    try {
                        user.setRoles(objectMapper.readValue(rolesJson, new TypeReference<>() {}));
                        user.setPermissions(objectMapper.readValue(permsJson, new TypeReference<>() {}));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to map transient fields", e);
                    }

                    user.setProfileEntity(profileMapper.map(row));   // Optional: use a RowMapper<ProfileEntity>
                    user.setAddressEntity(addressMapper.map(row));   // Optional: use a RowMapper<AddressEntity>
                    return user;

                }).all();
            }
}
