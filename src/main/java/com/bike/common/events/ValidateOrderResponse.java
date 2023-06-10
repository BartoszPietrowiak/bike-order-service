package com.bike.common.events;

import com.bike.common.BikeOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOrderResponse {
    private UUID orderId;
    private Boolean isValid;
}
