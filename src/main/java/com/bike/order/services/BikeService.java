package com.bike.order.services;

import com.bike.common.BikeDto;

import java.util.Optional;
import java.util.UUID;

public interface BikeService {
    Optional<BikeDto> getBikeByUpc(String upc);

    Optional<BikeDto> getBikeById(UUID bikeId);
}
