package com.happy.VenueService.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.happy.VenueService.entity.TicketTier;
import com.happy.VenueService.entity.Venue;

import jakarta.persistence.criteria.Join;

// 1. 定义积木
public class VenueSpecifications {
	public static Specification<Venue> hasCity(String city) {
			// root: 代表 Venue 表。root.get("cityCode") 获取表里的 city_code 列
	    // cb (CriteriaBuilder): 查询工厂。equal 方法创建“等于”逻辑
	    // 相当于 SQL: WHERE city_code = 'SHANGHAI'
	    return (root, query, cb) -> 
	        (city == null || city.isEmpty()) ? null : cb.equal(root.get("cityCode"), city);
	}
	
	public static Specification<Venue> minPriceGreaterThan(BigDecimal minPrice) {
	    return (root, query, cb) -> {
	            if (minPrice == null) return null;
	            // join: 因为价格在 TicketTier 表中，我们需要关联查询
	            // 这行代码告诉 JPA：请把 Venue 表和它的 ticketTiers 集合关联（INNER JOIN）
	            Join<Venue, TicketTier> tiers = root.join("ticketTiers");
	            // 相当于 SQL: WHERE ticket_tiers.price >= 100.0
	            return cb.greaterThanOrEqualTo(tiers.get("price"), minPrice);
	    };
	}
    public static Specification<Venue> maxPriceLessThan(BigDecimal maxPrice) {
	    return (root, query, cb) -> {
	            if (maxPrice == null) return null;
	            Join<Venue, TicketTier> tiers = root.join("ticketTiers");
	            return cb.lessThanOrEqualTo(tiers.get("price"), maxPrice);
	    };
	}
    public static Specification<Venue> hasStartTimeBefore(OffsetDateTime startTimeBefore) {
        return (root, query, cb) -> {
                if (startTimeBefore == null) return null;
                return cb.lessThanOrEqualTo(root.get("startTime"), startTimeBefore);
        };
    }
    public static Specification<Venue> hasStartTimeAfter(OffsetDateTime startTimeAfter) {
        return (root, query, cb) -> {
                if (startTimeAfter == null) return null;
                return cb.greaterThanOrEqualTo(root.get("startTime"), startTimeAfter);
        };
    }
    public static Specification<Venue> hasCategoryIn(List<String> categories) {
        return (root, query, cb) -> {
                if (categories == null || categories.isEmpty()) return null;
                Join<Venue, TicketTier> tiers = root.join("ticketTiers");
                // SQL: WHERE ticket_tiers.category IN ('Music', 'Sports')
                return tiers.get("category").in(categories);
        };
    }
}
// usage
// public Window<Venue> getVenues(VenueFilter filter, ScrollPosition position) {
//     // 动态组合所有非空条件
//     Specification<Venue> spec = Specification.where(VenueSpecifications.hasCity(filter.getCityCode()))
//             .and(VenueSpecifications.hasPriceBetween(filter.getMinPrice(), filter.getMaxPrice()));
//             // .and(以后想加什么就加什么)

//     return venueRepository.findBy(spec, q -> q.limit(10).scroll(position));
// }