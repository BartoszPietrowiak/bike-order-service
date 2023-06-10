package com.bike.order.services;

import com.bike.common.BikeOrderDto;
import com.bike.common.BikeOrderPagedList;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BikeOrderService {
    BikeOrderPagedList listOrders(UUID customerId, Pageable pageable);

    BikeOrderDto placeOrder(UUID customerId, BikeOrderDto bikeOrderDto);

    BikeOrderDto getOrderById(UUID customerId, UUID orderId);

    void pickupOrder(UUID customerId, UUID orderId);
}
