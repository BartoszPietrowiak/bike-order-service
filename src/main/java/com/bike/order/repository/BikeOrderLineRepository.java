package com.bike.order.repository;

import com.bike.order.domain.BikeOrderLine;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface BikeOrderLineRepository extends PagingAndSortingRepository<BikeOrderLine, UUID> {
}

