package com.bike.order.repository;

import com.bike.order.domain.BikeOrder;
import com.bike.order.domain.BikeOrderStatusEnum;
import com.bike.order.domain.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.UUID;

public interface BikeOrderRepository extends JpaRepository<BikeOrder, UUID> {
    Page<BikeOrder> findAllByCustomer(Customer customer, Pageable pageable);

    List<BikeOrder> findAllByOrderStatus(BikeOrderStatusEnum bikeOrderStatusEnum);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    BikeOrder findOneById(UUID id);
}

