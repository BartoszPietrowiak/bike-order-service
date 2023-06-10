package com.bike.common.events;

import com.bike.common.BikeOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateOrderResponse {
    private BikeOrderDto bikeOrderDto;
    private boolean allocationError = false;
    private boolean inventoryPending = false;
}
