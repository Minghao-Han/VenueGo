package com.happy.VenueService.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.happy.VenueService.entity.TicketTier;
import com.happy.VenueService.entity.Venue;

import jakarta.persistence.criteria.Join;

// Specification building blocks.
public class VenueSpecifications {
	public static Specification<Venue> hasCity(String city) {
			// root points to Venue; root.get("cityCode") maps to the city_code column.
	    // cb (CriteriaBuilder) creates predicates such as equality.
	    // Equivalent SQL: WHERE city_code = 'SHANGHAI'
	    return (root, query, cb) -> 
	        (city == null || city.isEmpty()) ? null : cb.equal(root.get("cityCode"), city);
	}
	
	public static Specification<Venue> minPriceGreaterThan(BigDecimal minPrice) {
	    return (root, query, cb) -> {
	            if (minPrice == null) return null;
	            // Price is stored on TicketTier, so a join is required.
	            // This produces an inner join between Venue and ticketTiers.
	            Join<Venue, TicketTier> tiers = root.join("ticketTiers");
	            // Equivalent SQL: WHERE ticket_tiers.price >= 100.0
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
//     // Dynamically combine all non-null predicates.
//     Specification<Venue> spec = Specification.where(VenueSpecifications.hasCity(filter.getCityCode()))
//             .and(VenueSpecifications.hasPriceBetween(filter.getMinPrice(), filter.getMaxPrice()));
//             // .and(add additional predicates when needed)

//     return venueRepository.findBy(spec, q -> q.limit(10).scroll(position));
// }