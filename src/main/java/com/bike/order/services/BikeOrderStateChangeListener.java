package com.bike.order.services;

import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderEventEnum;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.repository.BikeOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BikeOrderStateChangeListener extends StateMachineInterceptorAdapter<BikeOrderStatusEnum, BikeOrderEventEnum> {

    private final BikeOrderRepository bikeOrderRepository;

    @Override
    @Transactional
    public void preStateChange(State<BikeOrderStatusEnum, BikeOrderEventEnum> state, Message<BikeOrderEventEnum> message,
                               Transition<BikeOrderStatusEnum, BikeOrderEventEnum> transition, StateMachine<BikeOrderStatusEnum, BikeOrderEventEnum> stateMachine,
                               StateMachine<BikeOrderStatusEnum, BikeOrderEventEnum> rootStateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(((String) msg.getHeaders().getOrDefault(BikeOrderManagerImpl.BIKE_ORDER_HEADER_ID, "")))
                    .ifPresent(orderId -> {
                        BikeOrder bikeOrder = bikeOrderRepository.getOne(UUID.fromString(orderId));
                        bikeOrder.setOrderStatus(state.getId());
                        bikeOrderRepository.saveAndFlush(bikeOrder);
                    });
        });
    }
}
