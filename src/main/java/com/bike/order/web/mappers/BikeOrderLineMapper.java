package com.bike.order.web.mappers;

import com.bike.common.BikeOrderLineDto;
import com.bike.order.domain.BikeOrderLine;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
@DecoratedWith(BikeOrderLineMapperDecorator.class)
public interface BikeOrderLineMapper {
    BikeOrderLineDto bikeOrderLineToDto(BikeOrderLine line);

    BikeOrderLine dtoToBikeOrderLine(BikeOrderLineDto dto);
}