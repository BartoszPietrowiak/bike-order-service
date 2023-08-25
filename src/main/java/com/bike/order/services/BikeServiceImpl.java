package com.bike.order.services;

import com.bike.common.BikeDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
public class BikeServiceImpl implements BikeService {

    public final static String BEER_PATH_V1 = "/api/v1/bike/";
    public final static String BEER_UPC_PATH_V1 = "/api/v1/bikeUpc/";
    private final RestTemplate restTemplate;

    @Value("${com.bike.order.bike-management-host:http://localhost:8083}")
    private String bikeManagementeHost;

    public BikeServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    @Override
    public Optional<BikeDto> getBikeByUpc(String upc) {
        return Optional.ofNullable(restTemplate.getForObject(bikeManagementeHost + BEER_UPC_PATH_V1 + upc, BikeDto.class));
    }

    @Override
    public Optional<BikeDto> getBikeById(UUID bikeId) {
        return Optional.ofNullable(restTemplate.getForObject(bikeManagementeHost + BEER_PATH_V1 + bikeId.toString(), BikeDto.class));
    }

    public void setBeerServiceHost(String bikeManagementeHost) {
        this.bikeManagementeHost = bikeManagementeHost;
    }
}
