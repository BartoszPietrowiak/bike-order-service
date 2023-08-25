package com.bike.order.components;

import com.bike.common.events.AllocateOrderRequest;
import com.bike.common.events.AllocateOrderResponse;
import com.bike.order.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BikeOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg) {
        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();
        boolean pendingInventory = false;
        boolean allocationError = false;
        boolean sendResponse = true;

        //set allocation error
        if (request.getBikeOrderDto().getCustomerRef() != null) {
            switch (request.getBikeOrderDto().getCustomerRef()) {
                case "fail-allocation" -> allocationError = true;
                case "partial-allocation" -> pendingInventory = true;
                case "dont-allocate" -> sendResponse = false;
            }
        }

        boolean finalPendingInventory = pendingInventory;

        request.getBikeOrderDto().getBikeOrderLine().forEach(bikeOrderLineDto -> {
            if (finalPendingInventory) {
                bikeOrderLineDto.setAllocatedQuantity(bikeOrderLineDto.getOrderQuantity() - 1);
            } else {
                bikeOrderLineDto.setAllocatedQuantity(bikeOrderLineDto.getOrderQuantity());
            }
        });

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                    AllocateOrderResponse.builder()
                            .bikeOrderDto(request.getBikeOrderDto())
                            .inventoryPending(pendingInventory)
                            .allocationError(allocationError)
                            .build());
        }
    }
}
