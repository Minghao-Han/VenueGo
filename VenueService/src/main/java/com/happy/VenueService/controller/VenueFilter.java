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
@Data // 使用 Lombok 自动生成 Getter/Setter/ToString/默认构造函数
public class VenueFilter {
    // 这些字段名必须与 graphqls 文件中的 input VenueFilter 字段名完全一致
    // These fields must match the field names in the graphqls input VenueFilter exactly
    private String cityCode;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> categories;
    private OffsetDateTime startTimeAfter;
    private OffsetDateTime startTimeBefore;
}