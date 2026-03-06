package com.happy.VenueService.controller;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/*
input VenueFilter {
    cityCode: String
    minPrice: BigDecimal
    maxPrice: BigDecimal
    startTimeBefore: DateTime
    startTimeAfter: DateTime
    categories: [String]
}
*/
@Data // Lombok generates getters, setters, toString, and constructors.
public class VenueFilter {
    // These fields must exactly match the GraphQL schema input VenueFilter fields.
    private String cityCode;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> categories;
    private OffsetDateTime startTimeAfter;
    private OffsetDateTime startTimeBefore;
}