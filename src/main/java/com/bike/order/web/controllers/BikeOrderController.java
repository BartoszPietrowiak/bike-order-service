package com.bike.order.web.controllers;

import com.bike.common.BikeOrderDto;
import com.bike.common.BikeOrderPagedList;
import com.bike.order.services.BikeOrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/customers/{customerId}/")
@RestController
public class BikeOrderController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final BikeOrderService bikeOrderService;

    public BikeOrderController(BikeOrderService bikeOrderService) {
        this.bikeOrderService = bikeOrderService;
    }

    @GetMapping("orders")
    public BikeOrderPagedList listOrders(@PathVariable("customerId") UUID customerId,
                                         @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize){

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return bikeOrderService.listOrders(customerId, PageRequest.of(pageNumber, pageSize));
    }

    @PostMapping("orders")
    @ResponseStatus(HttpStatus.CREATED)
    public BikeOrderDto placeOrder(@PathVariable("customerId") UUID customerId, @RequestBody BikeOrderDto bikeOrderDto){
        return bikeOrderService.placeOrder(customerId, bikeOrderDto);
    }

    @GetMapping("orders/{orderId}")
    public BikeOrderDto getOrder(@PathVariable("customerId") UUID customerId, @PathVariable("orderId") UUID orderId){
        return bikeOrderService.getOrderById(customerId, orderId);
    }

    @PutMapping("/orders/{orderId}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickupOrder(@PathVariable("customerId") UUID customerId, @PathVariable("orderId") UUID orderId){
        bikeOrderService.pickupOrder(customerId, orderId);
    }
}