package com.bike.order.bootstrap;

import com.bike.order.domain.Customer;
import com.bike.order.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class BikeOrderBootstrap implements CommandLineRunner {
    public static final String TASTING_ROOM = "Tasting Room";
    public static final String BIKE_1_UPC = "0631234200036";
    public static final String BIKE_2_UPC = "0631234300019";
    public static final String BIKE_3_UPC = "0083783375213";

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        loadCustomerData();
    }

    private void loadCustomerData() {
        if (customerRepository.findAllByCustomerNameLike(BikeOrderBootstrap.TASTING_ROOM).size() == 0) {
            Customer savedCustomer = customerRepository.saveAndFlush(Customer.builder()
                    .customerName(TASTING_ROOM)
                    .apiKey(UUID.randomUUID())
                    .build());
            log.info(savedCustomer.getId().toString());
        }
    }
}
