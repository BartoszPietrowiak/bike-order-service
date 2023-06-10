package com.bike.order.services;

import com.bike.common.BikeOrderDto;
import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderEventEnum;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.repository.BikeOrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BikeOrderManagerImpl implements BikeOrderManager {

    public static final String BIKE_ORDER_HEADER_ID = "bike_order_id";
    private final StateMachineFactory<BikeOrderStatusEnum, BikeOrderEventEnum> stateMachineFactory;
    private final BikeOrderRepository bikeOrderRepository;
    private final BikeOrderStateChangeListener bikeOrderStateChangeListener;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BikeOrder newBikeOrder(BikeOrder bikeOrder) {
        bikeOrder.setId(null);
        bikeOrder.setOrderStatus(BikeOrderStatusEnum.NEW);

        BikeOrder savedBikeOrder = bikeOrderRepository.save(bikeOrder);

        sendBikeOrderEvent(savedBikeOrder, BikeOrderEventEnum.VALIDATE_ORDER);
        return savedBikeOrder;
    }

    @Override
    @Transactional
    public void processValidationResult(UUID bikeOrderId, Boolean isValid) {
        Optional<BikeOrder> bikeOrderOptional = bikeOrderRepository.findById(bikeOrderId);
        bikeOrderOptional.ifPresentOrElse(bikeOrder -> {
            if (isValid) {
                sendBikeOrderEvent(bikeOrder, BikeOrderEventEnum.VALIDATION_PASS);
                awaitForStatus(bikeOrderId, BikeOrderStatusEnum.VALIDATED);
                BikeOrder validatedOrder = bikeOrderRepository.findById(bikeOrderId).get();
                sendBikeOrderEvent(validatedOrder, BikeOrderEventEnum.ALLOCATE_ORDER);
            } else {
                sendBikeOrderEvent(bikeOrder, BikeOrderEventEnum.VALIDATION_FAILED);
            }
        }, () -> log.error("Order not found Id:" + bikeOrderId));
    }

    @Override
    public void bikeOrderAllocationPassed(BikeOrderDto bikeOrderDto) {
        Optional<BikeOrder> bikeOrderOptional = bikeOrderRepository.findById(bikeOrderDto.getId());
        bikeOrderOptional.ifPresentOrElse(bikeOrder -> {
            sendBikeOrderEvent(bikeOrder, BikeOrderEventEnum.ALLOCATION_SUCCESS);
            awaitForStatus(bikeOrder.getId(), BikeOrderStatusEnum.ALLOCATED);
            updateAllocatedQty(bikeOrderDto);
        }, () -> log.error("Order not found Id:" + bikeOrderDto.getId()));
    }

    @Override
    public void bikeOrderAllocationPendingInventory(BikeOrderDto bikeOrderDto) {
        Optional<BikeOrder> bikeOrderOptional = bikeOrderRepository.findById(bikeOrderDto.getId());
        bikeOrderOptional.ifPresentOrElse(bikeOrder -> {
            sendBikeOrderEvent(bikeOrder, BikeOrderEventEnum.ALLOCATION_NO_INVENTORY);
            awaitForStatus(bikeOrder.getId(), BikeOrderStatusEnum.PENDING_INVENTORY);
            updateAllocatedQty(bikeOrderDto);
        }, () -> log.error("Order not found Id:" + bikeOrderDto.getId()));
    }

    @Override
    public void bikeOrderAllocationFailed(BikeOrderDto bikeOrderDto) {
        Optional<BikeOrder> bikeOrderOptional = bikeOrderRepository.findById(bikeOrderDto.getId());
        bikeOrderOptional.ifPresentOrElse(bikeOrder -> {
            sendBikeOrderEvent(bikeOrder, BikeOrderEventEnum.ALLOCATION_FAILED);
        }, () -> log.error("Order not found Id:" + bikeOrderDto.getId()));
    }

    @Override
    public void bikeOrderPickedUp(UUID bikeOrderId) {
        Optional<BikeOrder> bikeOrderOptional = bikeOrderRepository.findById(bikeOrderId);
        bikeOrderOptional.ifPresentOrElse(bikeOrder -> {
            sendBikeOrderEvent(bikeOrder, BikeOrderEventEnum.BIKE_ORDER_PICKED_UP);
        }, () -> log.error("Order not found Id:" + bikeOrderId));
    }

    @Override
    @Transactional
    public void bikeOrderCancelled(UUID bikeOrderId) {
        Optional<BikeOrder> bikeOrderOptional = bikeOrderRepository.findById(bikeOrderId);
        bikeOrderOptional.ifPresentOrElse(bikeOrder -> {
            sendBikeOrderEvent(bikeOrder, BikeOrderEventEnum.CANCEL_ORDER);
        }, () -> log.error("Order not found Id:" + bikeOrderId));
    }

    private void updateAllocatedQty(BikeOrderDto bikeOrderDto) {
        Optional<BikeOrder> bikeOrderOptional = bikeOrderRepository.findById(bikeOrderDto.getId());
        bikeOrderOptional.ifPresentOrElse(allocatedOrder -> {
            allocatedOrder.getBikeOrderLine().forEach(bikeOrderLine -> {
                bikeOrderDto.getBikeOrderLines().forEach(bikeOrderLineDto -> {
                    if (bikeOrderLine.getId().equals(bikeOrderLineDto.getId())) {
                        bikeOrderLine.setAllocatedQuantity(bikeOrderLineDto.setAllocatedQuantity());
                    }
                });
            });
            bikeOrderRepository.saveAndFlush(allocatedOrder);
        }, () -> log.error("Order not found Id:" + bikeOrderDto.getId()));

    }

    private void sendBikeOrderEvent(BikeOrder bikeOrder, BikeOrderEventEnum eventEnum) {
        StateMachine<BikeOrderStatusEnum, BikeOrderEventEnum> sm = build(bikeOrder);

        Message msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(BEER_ORDER_HEADER_ID, bikeOrder.getId().toString()).build();

        sm.sendEvent(msg);
    }

    private void awaitForStatus(UUID bikeOrderId, BikeOrderStatusEnum statusEnum) {
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop retries exceeded");
            }
            bikeOrderRepository.findById(bikeOrderId).ifPresentOrElse(bikeOrder -> {
                if (bikeOrder.getOrderStatus().equals(statusEnum)) {
                    found.set(true);
                    log.debug("Order found");
                } else {
                    log.debug("Order Status not equal");

                }
            }, () -> {
                log.debug("Order Id not found");
            });
            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {
                    //do nothing
                }
            }
        }
    }

    private StateMachine<BikeOrderStatusEnum, BikeOrderEventEnum> build(BikeOrder bikeOrder) {
        StateMachine<BikeOrderStatusEnum, BikeOrderEventEnum> sm = stateMachineFactory.getStateMachine(bikeOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(bikeOrderStateChangeListener);
            sma.resetStateMachine(new DefaultStateMachineContext<>(bikeOrder.getOrderStatus(), null, null, null));
        });

        sm.start();
        return sm;
    }
}
