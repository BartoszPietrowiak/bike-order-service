package com.bike.order.services.listeners;

import com.bike.common.events.ValidateOrderResponse;
import com.bike.order.config.JmsConfig;
import com.bike.order.services.BikeOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationResultListener {

    private final BikeOrderManager bikeOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidateOrderResponse response) {
        final UUID bikeOrderId = response.getOrderId();
        log.debug("Validation result for Order Id: " + bikeOrderId);

        bikeOrderManager.processValidationResult(bikeOrderId, response.getIsValid());
    }

}
