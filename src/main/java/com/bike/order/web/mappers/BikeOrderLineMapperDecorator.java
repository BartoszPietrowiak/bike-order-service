package com.bike.order.web.mappers;

import com.bike.common.BikeDto;
import com.bike.common.BikeOrderLineDto;
import com.bike.order.domain.BikeOrderLine;
import com.bike.order.services.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

public abstract class BikeOrderLineMapperDecorator implements BikeOrderLineMapper {

    private BikeService bikeService;
    private BikeOrderLineMapper bikeOrderLineMapper;

    @Autowired
    public void setBikeService(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @Autowired
    @Qualifier("delegate")
    public void setBikeOrderLineMapper(BikeOrderLineMapper bikeOrderLineMapper) {
        this.bikeOrderLineMapper = bikeOrderLineMapper;
    }

    @Override
    public BikeOrderLineDto bikeOrderLineToDto(BikeOrderLine line) {
        BikeOrderLineDto orderLineDto = bikeOrderLineMapper.bikeOrderLineToDto(line);
        Optional<BikeDto> bikeDtoOptional = bikeService.getBikeByUpc(line.getUpc());

        bikeDtoOptional.ifPresent(bikeDto -> {
            orderLineDto.setBikeName(bikeDto.getBikeName());
            orderLineDto.setBikeType(bikeDto.getBikeType());
            orderLineDto.setPrice(bikeDto.getPrice());
            orderLineDto.setBikeId(bikeDto.getId());
        });
        return orderLineDto;
    }

    @Override
    public BikeOrderLine dtoToBikeOrderLine(BikeOrderLineDto dto) {
        return bikeOrderLineMapper.dtoToBikeOrderLine(dto);
    }

}