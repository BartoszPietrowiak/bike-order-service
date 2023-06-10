package com.bike.order.web.mappers;

import com.bike.common.BikeOrderDto;
import com.bike.order.domain.BikeOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class, BikeOrderLineMapper.class})
public interface BikeOrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    BikeOrderDto bikeOrderToDto(BikeOrder bikeOrder);

    BikeOrder dtoToBikeOrder(BikeOrderDto dto);
}