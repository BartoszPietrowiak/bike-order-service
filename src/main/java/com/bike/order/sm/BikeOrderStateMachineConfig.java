package com.bike.order.sm;

import com.bike.common.BikeOrderDto;
import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderEventEnum;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.repository.BikeOrderRepository;
import com.bike.order.services.BikeOrderManager;
import com.bike.order.services.BikeOrderStateChangeListener;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class BikeOrderStateMachineConfig extends StateMachineConfigurerAdapter<BikeOrderStatusEnum, BikeOrderEventEnum> {

    private final Action<BikeOrderStatusEnum,BikeOrderEventEnum> validateOrderAction;
    private final Action<BikeOrderStatusEnum,BikeOrderEventEnum> allocateOrderAction;
    private final Action<BikeOrderStatusEnum,BikeOrderEventEnum> validationFailureAction;
    private final Action<BikeOrderStatusEnum,BikeOrderEventEnum> allocationFailureAction;
    private final Action<BikeOrderStatusEnum,BikeOrderEventEnum> deallocationAction;

    @Override
    public void configure(StateMachineStateConfigurer<BikeOrderStatusEnum, BikeOrderEventEnum> states) throws Exception {
        states.withStates()
                .initial(BikeOrderStatusEnum.NEW)
                .states(EnumSet.allOf(BikeOrderStatusEnum.class))
                .end(BikeOrderStatusEnum.PICKED_UP)
                .end(BikeOrderStatusEnum.DELIVERED)
                .end(BikeOrderStatusEnum.DELIVERY_EXCEPTION)
                .end(BikeOrderStatusEnum.VALIDATION_EXCEPTION)
                .end(BikeOrderStatusEnum.ALLOCATION_EXCEPTION)
                .end(BikeOrderStatusEnum.CANCELED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BikeOrderStatusEnum, BikeOrderEventEnum> transitions) throws Exception {
        transitions.withExternal()
                .source(BikeOrderStatusEnum.NEW)
                .target(BikeOrderStatusEnum.PENDING_VALIDATION)
                .event(BikeOrderEventEnum.VALIDATE_ORDER)
                .action(validateOrderAction)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.PENDING_VALIDATION)
                .target(BikeOrderStatusEnum.VALIDATED)
                .event(BikeOrderEventEnum.VALIDATION_PASS)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.PENDING_VALIDATION)
                .target(BikeOrderStatusEnum.CANCELED)
                .event(BikeOrderEventEnum.CANCEL_ORDER)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.PENDING_VALIDATION)
                .target(BikeOrderStatusEnum.VALIDATION_EXCEPTION)
                .event(BikeOrderEventEnum.VALIDATION_FAILED)
                .action(validationFailureAction)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.VALIDATED)
                .target(BikeOrderStatusEnum.PENDING_ALLOCATION)
                .event(BikeOrderEventEnum.ALLOCATE_ORDER)
                .action(allocateOrderAction)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.VALIDATED)
                .target(BikeOrderStatusEnum.CANCELED)
                .event(BikeOrderEventEnum.CANCEL_ORDER)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.PENDING_ALLOCATION)
                .target(BikeOrderStatusEnum.ALLOCATED)
                .event(BikeOrderEventEnum.ALLOCATION_SUCCESS)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.PENDING_ALLOCATION)
                .target(BikeOrderStatusEnum.CANCELED)
                .event(BikeOrderEventEnum.CANCEL_ORDER)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.PENDING_ALLOCATION)
                .target(BikeOrderStatusEnum.ALLOCATION_EXCEPTION)
                .event(BikeOrderEventEnum.ALLOCATION_FAILED)
                .action(allocationFailureAction)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.PENDING_ALLOCATION)
                .target(BikeOrderStatusEnum.PENDING_INVENTORY)
                .event(BikeOrderEventEnum.ALLOCATION_NO_INVENTORY)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.ALLOCATED)
                .target(BikeOrderStatusEnum.PICKED_UP)
                .event(BikeOrderEventEnum.BIKE_ORDER_PICKED_UP)
                .and()
                .withExternal()
                .source(BikeOrderStatusEnum.ALLOCATED)
                .target(BikeOrderStatusEnum.CANCELED)
                .event(BikeOrderEventEnum.CANCEL_ORDER)
                .action(deallocationAction);
    }
}
