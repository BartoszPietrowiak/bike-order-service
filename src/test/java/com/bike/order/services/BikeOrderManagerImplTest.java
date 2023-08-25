package com.bike.order.services;

import com.bike.common.BikeDto;
import com.bike.common.events.AllocationFailedRequest;
import com.bike.common.events.DeallocateOrderRequest;
import com.bike.order.config.JmsConfig;
import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderLine;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.domain.Customer;
import com.bike.order.repository.BikeOrderRepository;
import com.bike.order.repository.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BikeOrderManagerImplTest {

    @Autowired
    BikeOrderManager bikeOrderManager;

    @Autowired
    BikeOrderRepository bikeOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    JmsTemplate jmsTemplate;

    Customer testCustomer;

    UUID bikeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
                .customerName("Test Customer")
                .build());
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException, InterruptedException {
        BikeDto bikeDto = BikeDto.builder().id(bikeId).upc("12345").build();

        wireMockServer.stubFor(get(BikeServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(bikeDto))));

        BikeOrder bikeOrder = createBikeOrder();

        BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();

            assertEquals(BikeOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            BikeOrderLine line = foundOrder.getBikeOrderLine().iterator().next();
            assertEquals(line.getOrderQuantity(), line.getAllocatedQuantity());
        });

        BikeOrder savedBikeOrder2 = bikeOrderRepository.findById(savedBikeOrder.getId()).get();

        assertNotNull(savedBikeOrder2);
        assertEquals(BikeOrderStatusEnum.ALLOCATED, savedBikeOrder2.getOrderStatus());
        savedBikeOrder2.getBikeOrderLine().forEach(line -> {
            assertEquals(line.getOrderQuantity(), line.getAllocatedQuantity());
        });
    }

    @Test
    void testFailedValidation() throws JsonProcessingException {
        BikeDto bikeDto = BikeDto.builder().id(bikeId).upc("12345").build();

        wireMockServer.stubFor(get(BikeServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(bikeDto))));

        BikeOrder bikeOrder = createBikeOrder();
        bikeOrder.setCustomerRef("fail-validation");

        BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();

            assertEquals(BikeOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        BikeDto bikeDto = BikeDto.builder().id(bikeId).upc("12345").build();

        wireMockServer.stubFor(get(BikeServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(bikeDto))));

        BikeOrder bikeOrder = createBikeOrder();

        BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        bikeOrderManager.bikeOrderPickedUp(savedBikeOrder.getId());

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
        });

        BikeOrder pickedUpOrder = bikeOrderRepository.findById(savedBikeOrder.getId()).get();

        assertEquals(BikeOrderStatusEnum.PICKED_UP, pickedUpOrder.getOrderStatus());
    }

    @Test
    void testAllocationFailure() throws JsonProcessingException {
        BikeDto bikeDto = BikeDto.builder().id(bikeId).upc("12345").build();

        wireMockServer.stubFor(get(BikeServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(bikeDto))));

        BikeOrder bikeOrder = createBikeOrder();
        bikeOrder.setCustomerRef("fail-allocation");

        BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
        });

        AllocationFailedRequest allocationFailureEvent = (AllocationFailedRequest) jmsTemplate.receiveAndConvert(JmsConfig.ALLOCATE_ORDER_FAILED_QUEUE);

        assertNotNull(allocationFailureEvent);
        assertThat(allocationFailureEvent.getOrderId()).isEqualTo(savedBikeOrder.getId());
    }

    @Test
    void testPartialAllocation() throws JsonProcessingException {
        BikeDto bikeDto = BikeDto.builder().id(bikeId).upc("12345").build();

        wireMockServer.stubFor(get(BikeServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(bikeDto))));

        BikeOrder bikeOrder = createBikeOrder();
        bikeOrder.setCustomerRef("partial-allocation");

        BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.PENDING_INVENTORY, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testValidationPendingToCancel() throws JsonProcessingException {
        BikeDto bikeDto = BikeDto.builder().id(bikeId).upc("12345").build();

        wireMockServer.stubFor(get(BikeServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(bikeDto))));

        BikeOrder bikeOrder = createBikeOrder();
        bikeOrder.setCustomerRef("dont-validate");

        BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.PENDING_VALIDATION, foundOrder.getOrderStatus());
        });

        bikeOrderManager.bikeOrderCancelled(savedBikeOrder.getId());

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.CANCELED, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testAllocationPendingToCancel() throws JsonProcessingException {
        BikeDto bikeDto = BikeDto.builder().id(bikeId).upc("12345").build();

        wireMockServer.stubFor(get(BikeServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(bikeDto))));

        BikeOrder bikeOrder = createBikeOrder();
        bikeOrder.setCustomerRef("dont-allocate");

        BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.PENDING_ALLOCATION, foundOrder.getOrderStatus());
        });

        bikeOrderManager.bikeOrderCancelled(savedBikeOrder.getId());

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.CANCELED, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testAllocatedToCancel() throws JsonProcessingException {
        BikeDto bikeDto = BikeDto.builder().id(bikeId).upc("12345").build();

        wireMockServer.stubFor(get(BikeServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(bikeDto))));

        BikeOrder bikeOrder = createBikeOrder();

        BikeOrder savedBikeOrder = bikeOrderManager.newBikeOrder(bikeOrder);

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        bikeOrderManager.bikeOrderCancelled(savedBikeOrder.getId());

        await().untilAsserted(() -> {
            BikeOrder foundOrder = bikeOrderRepository.findById(bikeOrder.getId()).get();
            assertEquals(BikeOrderStatusEnum.CANCELED, foundOrder.getOrderStatus());
        });

        DeallocateOrderRequest deallocateOrderRequest = (DeallocateOrderRequest) jmsTemplate.receiveAndConvert(JmsConfig.DEALLOCATE_ORDER_QUEUE);

        assertNotNull(deallocateOrderRequest);
        assertThat(deallocateOrderRequest.getBikeOrderDto().getId()).isEqualTo(savedBikeOrder.getId());
    }

    public BikeOrder createBikeOrder() {
        BikeOrder bikeOrder = BikeOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BikeOrderLine> lines = new HashSet<>();
        lines.add(BikeOrderLine.builder()
                .bikeId(bikeId)
                .upc("12345")
                .orderQuantity(1)
                .bikeOrder(bikeOrder)
                .build());

        bikeOrder.setBikeOrderLine(lines);

        return bikeOrder;
    }

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = new WireMockServer(options().port(8083));
            server.start();
            return server;
        }
    }
}
