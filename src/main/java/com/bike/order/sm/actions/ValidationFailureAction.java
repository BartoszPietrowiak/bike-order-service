package com.bike.order.sm.actions;

import com.bike.common.events.ValidateOrderRequest;
import com.bike.order.config.JmsConfig;
import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderEventEnum;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.repository.BikeOrderRepository;
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
public class ValidationFailureAction implements Action<BikeOrderStatusEnum, BikeOrderEventEnum> {

    @Override
    public void execute(StateContext<BikeOrderStatusEnum, BikeOrderEventEnum> stateContext) {
        String bikeOrderId = (String) stateContext.getMessage().getHeaders().get(BikeOrderManagerImpl.BIKE_ORDER_HEADER_ID);
        log.error("Compoensating Transaction... Validation Failed:" + bikeOrderId);
    }
}
