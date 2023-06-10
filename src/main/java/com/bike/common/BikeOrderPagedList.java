package com.bike.common;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class BikeOrderPagedList extends PageImpl<BikeOrderDto> {
    public BikeOrderPagedList(List<BikeOrderDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public BikeOrderPagedList(List<BikeOrderDto> content) {
        super(content);
    }
}