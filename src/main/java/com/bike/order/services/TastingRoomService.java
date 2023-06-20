package com.bike.order.services;

import com.bike.common.BikeOrderDto;
import com.bike.common.BikeOrderLineDto;
import com.bike.common.CustomerPagedList;
import com.bike.order.bootstrap.BikeOrderBootstrap;
import com.bike.order.domain.Customer;
import com.bike.order.repository.BikeOrderRepository;
import com.bike.order.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final BikeOrderService bikeOrderService;
    private final BikeOrderRepository bikeOrderRepository;
    private final List<String> bikeUpcs = new ArrayList<>(3);

    public TastingRoomService(CustomerRepository customerRepository, BikeOrderService bikeOrderService,
                              BikeOrderRepository bikeOrderRepository) {
        this.customerRepository = customerRepository;
        this.bikeOrderService = bikeOrderService;
        this.bikeOrderRepository = bikeOrderRepository;

        bikeUpcs.add(BikeOrderBootstrap.BIKE_1_UPC);
        bikeUpcs.add(BikeOrderBootstrap.BIKE_2_UPC);
        bikeUpcs.add(BikeOrderBootstrap.BIKE_3_UPC);
    }

    @Transactional
    @Scheduled(fixedRate = 10000) //run every 10 seconds
    public void placeTastingRoomOrder() {

        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(BikeOrderBootstrap.TASTING_ROOM);

        if (customerList.size() == 1) { //should be just one
            doPlaceOrder(customerList.get(0));
        } else {
            log.error("Too many or too few tasting room customers found");
        }
    }

    private void doPlaceOrder(Customer customer) {
        String bikeToOrder = getRandomBikeUpc();

        BikeOrderLineDto bikeOrderLine = BikeOrderLineDto.builder()
                .upc(bikeToOrder)
                .orderQuantity(new Random().nextInt(6))
                .build();

        List<BikeOrderLineDto> bikeOrderLineSet = new ArrayList<>();
        bikeOrderLineSet.add(bikeOrderLine);

        BikeOrderDto bikeOrder = BikeOrderDto.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
                .bikeOrderLine(bikeOrderLineSet)
                .build();

        BikeOrderDto savedOrder = bikeOrderService.placeOrder(customer.getId(), bikeOrder);

    }

    private String getRandomBikeUpc() {
        return bikeUpcs.get(new Random().nextInt(bikeUpcs.size() - 0));
    }
}
