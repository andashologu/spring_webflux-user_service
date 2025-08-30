package com.trademarket.api.security.users.addresses.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.trademarket.api.security.users.addresses.model.AddressEntity;

import reactor.core.publisher.Flux;

public interface AddressRepository extends ReactiveCrudRepository<AddressEntity, Long> {

    Flux<AddressEntity> findByUserId(Long userId);
}
