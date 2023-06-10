package com.bike.order.sm.actions;

import com.bike.common.events.AllocationFailedRequest;
import com.bike.order.config.JmsConfig;
import com.bike.order.domain.BikeOrderEventEnum;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.services.BikeOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationFailureAction implements Action<BikeOrderStatusEnum, BikeOrderEventEnum> {
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BikeOrderStatusEnum, BikeOrderEventEnum> stateContext) {
        String bikeOrderId = (String) stateContext.getMessage().getHeaders().get(BikeOrderManagerImpl.BEER_ORDER_HEADER_ID);


        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_FAILED_QUEUE, AllocationFailedRequest
                .builder()
                .orderId(UUID.fromString(bikeOrderId))
                .build());

        log.debug("Sent allocate failed request to queue for order id " + bikeOrderId);
    }
}
