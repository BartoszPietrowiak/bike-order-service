package com.bike.order.web.mappers;

import com.bike.common.BikeOrderLineDto;
import com.bike.common.CustomerDto;
import com.bike.order.domain.BikeOrderLine;
import com.bike.order.domain.Customer;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

    CustomerDto customerToDto(Customer customer);

    Customer dtoToCustomer(CustomerDto dto);
}