package com.bike.order.services.listeners;

import com.bike.common.BikeOrderDto;
import com.bike.common.events.AllocateOrderResponse;
import com.bike.order.config.JmsConfig;
import com.bike.order.domain.BikeOrder;
import com.bike.order.services.BikeOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationResponseListener {
    private final BikeOrderManager bikeOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResponse response) {
        if (response.isAllocationError() && !response.isInventoryPending()) {
            bikeOrderManager.bikeOrderAllocationFailed(response.getBikeOrderDto());
        } else if (!response.isAllocationError() && response.isInventoryPending()) {
            bikeOrderManager.bikeOrderAllocationPendingInventory(response.getBikeOrderDto());
        } else if (!response.isAllocationError() && !response.isInventoryPending()) {
            bikeOrderManager.bikeOrderAllocationPassed(response.getBikeOrderDto());
        }
    }
}
