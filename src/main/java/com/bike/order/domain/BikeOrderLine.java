package com.bike.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class BikeOrderLine extends BaseEntity {

    @ManyToOne
    private BikeOrder bikeOrder;
    private UUID bikeId;
    private String upc;
    private Integer orderQuantity = 0;
    private Integer allocatedQuantity = 0;

    @Builder
    public BikeOrderLine(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate,
                         BikeOrder bikeOrder, UUID bikeId, String upc, Integer orderQuantity,
                         Integer allocatedQuantity) {
        super(id, version, createdDate, lastModifiedDate);
        this.bikeOrder = bikeOrder;
        this.bikeId = bikeId;
        this.orderQuantity = orderQuantity;
        this.allocatedQuantity = allocatedQuantity;
        this.upc = upc;
    }
}
