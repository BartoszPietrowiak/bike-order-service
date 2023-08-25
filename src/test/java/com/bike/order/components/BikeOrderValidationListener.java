package com.bike.order.components;

import com.bike.common.events.ValidateOrderRequest;
import com.bike.common.events.ValidateOrderResponse;
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
public class BikeOrderValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void list(Message msg) {
        boolean isValid = true;
        boolean sendResponse = true;

        ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

        //condition to fail validation
        if (request.getBikeOrderDto().getCustomerRef() != null) {
            if (request.getBikeOrderDto().getCustomerRef().equals("fail-validation")) {
                isValid = false;
            } else if (request.getBikeOrderDto().getCustomerRef().equals("dont-validate")) {
                sendResponse = false;
            }
        }

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                    ValidateOrderResponse.builder()
                            .isValid(isValid)
                            .orderId(request.getBikeOrderDto().getId())
                            .build());
        }
    }
}
