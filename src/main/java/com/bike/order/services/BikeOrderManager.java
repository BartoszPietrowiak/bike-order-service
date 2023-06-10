package com.bike.order.services;

import com.bike.common.BikeOrderDto;
import com.bike.order.domain.BikeOrder;

import java.util.UUID;

public interface BikeOrderManager {
    BikeOrder newBikeOrder(BikeOrder bikeOrder);

    void processValidationResult(UUID bikeOrderId, Boolean isValid);

    void bikeOrderAllocationPassed(BikeOrderDto bikeOrderDto);

    void bikeOrderAllocationPendingInventory(BikeOrderDto bikeOrderDto);

    void bikeOrderAllocationFailed(BikeOrderDto bikeOrderDto);

    void bikeOrderPickedUp(UUID bikeOrderId);

    void bikeOrderCancelled(UUID bikeOrderId);
}
