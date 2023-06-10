package com.bike.order.sm.actions;

import com.bike.common.events.AllocateOrderRequest;
import com.bike.order.config.JmsConfig;
import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderEventEnum;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.repository.BikeOrderRepository;
import com.bike.order.services.BikeOrderManagerImpl;
import com.bike.order.web.mappers.BikeOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocateOrderAction implements Action<BikeOrderStatusEnum, BikeOrderEventEnum> {
    private final BikeOrderRepository bikeOrderRepository;
    private final BikeOrderMapper bikeOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BikeOrderStatusEnum, BikeOrderEventEnum> stateContext) {
        BikeOrder bikeOrder = bikeOrderRepository.findOneById(UUID
                .fromString((String) Objects.requireNonNull(stateContext
                        .getMessage()
                        .getHeaders()
                        .get(BikeOrderManagerImpl.BIKE_ORDER_HEADER_ID))));

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_QUEUE, AllocateOrderRequest
                .builder()
                .bikeOrderDto(bikeOrderMapper.bikeOrderToDto(bikeOrder))
                .build());

        log.debug("Sent allocate request to queue for order id " + bikeOrder.getId());
    }
}
