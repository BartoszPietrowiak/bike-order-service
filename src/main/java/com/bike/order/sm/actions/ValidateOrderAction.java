package com.bike.order.sm.actions;

import com.bike.common.events.ValidateOrderRequest;
import com.bike.order.config.JmsConfig;
import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderEventEnum;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.repository.BikeOrderRepository;
import com.bike.order.services.BikeOrderManagerImpl;
import com.bike.order.web.mappers.BikeOrderMapper;
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
public class ValidateOrderAction implements Action<BikeOrderStatusEnum, BikeOrderEventEnum> {

    private final BikeOrderRepository bikeOrderRepository;
    private final BikeOrderMapper bikeOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BikeOrderStatusEnum, BikeOrderEventEnum> stateContext) {
        BikeOrder bikeOrder = bikeOrderRepository.findOneById(UUID
                .fromString((String) stateContext
                        .getMessage()
                        .getHeaders()
                        .get(BikeOrderManagerImpl.BIKE_ORDER_HEADER_ID)));
        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE, ValidateOrderRequest
                .builder()
                .bikeOrderDto(bikeOrderMapper.bikeOrderToDto(bikeOrder))
                .build());

    }
}
