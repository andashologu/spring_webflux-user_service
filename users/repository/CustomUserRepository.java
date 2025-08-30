package com.trademarket.api.security.users.repository;

import com.trademarket.api.security.users.model.UserEntity;

import reactor.core.publisher.Flux;

public interface CustomUserRepository {

    Flux<UserEntity> findAllPaginated(Long cursor, Integer size, String sortBy, String direction, String search, 
                        String country, String city, String region, String street);

}

