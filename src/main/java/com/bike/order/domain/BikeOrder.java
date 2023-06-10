package com.bike.order.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class BikeOrder extends BaseEntity {

    private String customerRef;
    @ManyToOne
    private Customer customer;
    @OneToMany(mappedBy = "bikeOrder", cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private Set<BikeOrderLine> bikeOrderLine;
    private BikeOrderStatusEnum orderStatus = BikeOrderStatusEnum.NEW;
    private String orderStatusCallbackUrl;
    @Builder
    public BikeOrder(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate, String customerRef, Customer customer,
                     Set<BikeOrderLine> bikeOrderLine, BikeOrderStatusEnum orderStatus,
                     String orderStatusCallbackUrl) {
        super(id, version, createdDate, lastModifiedDate);
        this.customerRef = customerRef;
        this.customer = customer;
        this.bikeOrderLine = bikeOrderLine;
        this.orderStatus = orderStatus;
        this.orderStatusCallbackUrl = orderStatusCallbackUrl;
    }
}
