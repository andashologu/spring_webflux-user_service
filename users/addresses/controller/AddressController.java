package com.trademarket.api.security.users.addresses.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.trademarket.api.security.users.addresses.model.AddressEntity;
import com.trademarket.api.security.users.addresses.repository.AddressRepository;
import com.trademarket.api.security.users.addresses.service.AddressService;
import com.trademarket.api.security.users.repository.UserRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;

    public AddressController(AddressRepository addressRepository, AddressService addressService, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.addressService = addressService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Mono<AddressEntity> createAddress(@RequestBody AddressEntity addressEntity) {
        Instant now = Instant.now();
        // Check userId is provided
        if (addressEntity.getUserId() == null) return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID must be provided"));
        return userRepository.findById(addressEntity.getUserId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + addressEntity.getUserId())))
            .flatMap(_ -> {
                if (addressEntity.getId() == null) {
                    // CREATE
                    addressEntity.setCreatedAt(now);
                    addressEntity.setUpdatedAt(now);
                    addressEntity.setAccessedAt(now);
                    return addressService.saveAddress(addressEntity);
                } else {
                    // UPDATE
                    return addressRepository.findById(addressEntity.getId())
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found with id: " + addressEntity.getId())))
                        .flatMap(_ -> {
                            addressEntity.setUpdatedAt(now);
                            addressEntity.setAccessedAt(now);
                            return addressService.saveAddress(addressEntity);
                        });
                }
            });
    }

    @GetMapping
    public Flux<AddressEntity> getAllAddresses(
        @RequestParam(required = false) Integer cursor,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String country,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String street
    ) {
        return addressService.getAll(cursor, limit, country, city, region, street);
    }

    @GetMapping("/{id}")
    public Mono<AddressEntity> getAddressById(@PathVariable Long id) {
        return addressRepository.findById(id);
    }

    @GetMapping("/user/{userId}")
    public Flux<AddressEntity> getAddressesByUserId(@PathVariable Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteAddress(@PathVariable Long id) {
        return addressRepository.deleteById(id).then();
    }

}

