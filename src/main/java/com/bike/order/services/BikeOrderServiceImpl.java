package com.bike.order.services;

import com.bike.common.BikeOrderDto;
import com.bike.common.BikeOrderPagedList;
import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.domain.Customer;
import com.bike.order.repository.BikeOrderRepository;
import com.bike.order.repository.CustomerRepository;
import com.bike.order.web.mappers.BikeOrderMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BikeOrderServiceImpl implements BikeOrderService {

    private final BikeOrderRepository bikeOrderRepository;
    private final CustomerRepository customerRepository;
    private final BikeOrderMapper bikeOrderMapper;

    private final BikeOrderManager bikeOrderManager;


    @Override
    public BikeOrderPagedList listOrders(UUID customerId, Pageable pageable) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isPresent()) {
            Page<BikeOrder> bikeOrderPage =
                    bikeOrderRepository.findAllByCustomer(customerOptional.get(), pageable);

            return new BikeOrderPagedList(bikeOrderPage
                    .stream()
                    .map(bikeOrderMapper::bikeOrderToDto)
                    .collect(Collectors.toList()), PageRequest.of(
                    bikeOrderPage.getPageable().getPageNumber(),
                    bikeOrderPage.getPageable().getPageSize()),
                    bikeOrderPage.getTotalElements());
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public BikeOrderDto placeOrder(UUID customerId, BikeOrderDto bikeOrderDto) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isPresent()) {
            BikeOrder bikeOrder = bikeOrderMapper.dtoToBikeOrder(bikeOrderDto);
            bikeOrder.setId(null); //should not be set by outside client
            bikeOrder.setCustomer(customerOptional.get());
            bikeOrder.setOrderStatus(BikeOrderStatusEnum.NEW);

            bikeOrder.getBikeOrderLine().forEach(line -> line.setBikeOrder(bikeOrder));

            BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);
            log.info("Saved Bike Order: " + bikeOrder.getId());

            return bikeOrderMapper.bikeOrderToDto(savedBikeOrder);
        }
        //todo add exception type
        throw new RuntimeException("Customer Not Found");
    }

    @Override
    public BikeOrderDto getOrderById(UUID customerId, UUID orderId) {
        return bikeOrderMapper.bikeOrderToDto(getOrder(customerId, orderId));
    }

    @Override
    public void pickupOrder(UUID customerId, UUID orderId) {
        bikeOrderManager.bikeOrderPickedUp(orderId);
    }

    private BikeOrder getOrder(UUID customerId, UUID orderId) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isPresent()) {
            Optional<BikeOrder> bikeOrderOptional = bikeOrderRepository.findById(orderId);

            if (bikeOrderOptional.isPresent()) {
                BikeOrder bikeOrder = bikeOrderOptional.get();

                // fall to exception if customer id's do not match - order not for customer
                if (bikeOrder.getCustomer().getId().equals(customerId)) {
                    return bikeOrder;
                }
            }
            throw new RuntimeException("Bike Order Not Found");
        }
        throw new RuntimeException("Customer Not Found");
    }
}
