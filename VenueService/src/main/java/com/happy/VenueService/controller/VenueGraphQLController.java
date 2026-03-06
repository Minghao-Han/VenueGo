package com.happy.VenueService.controller;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.happy.VenueService.entity.Venue;
import com.happy.VenueService.service.VenueService;

@Controller
public class VenueGraphQLController {

    private final VenueService venueService;

    // Constructor injection for service dependency.
    public VenueGraphQLController(VenueService venueService) {
        this.venueService = venueService;
    }

    /**
     * @Argument automatically maps GraphQL request arguments into Java objects.
     */
    @QueryMapping
    public VenueConnection venuesConnection(
            @Argument Integer first, 
            @Argument String after, 
            @Argument VenueFilter filter) {
        
        // handle cursor (ScrollPosition)
        // If after is null, request the first page; otherwise parse it as an ID-based cursor.
        ScrollPosition position = (after != null) 
            ? ScrollPosition.forward(Collections.singletonMap("id", UUID.fromString(after)))
            : ScrollPosition.offset();
        // Execute filtered query in service and assemble GraphQL connection response.
        Window<Venue> window = venueService.getVenues(filter, position, first);
        List<VenueEdge> edges = window.getContent().stream()
                .map(venue -> new VenueEdge(venue.getId().toString(), venue))
                .collect(Collectors.toList());
        String endCursor = edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor();
        PageInfo pageInfo = new PageInfo(window.hasNext(), endCursor);
        return new VenueConnection(edges, pageInfo);
    }

    @QueryMapping
    public Venue venueById(@Argument UUID id) {
        return venueService.getVenueById(id);
    }

    public static class VenueConnection {
        private final List<VenueEdge> edges;
        private final PageInfo pageInfo;

        public VenueConnection(List<VenueEdge> edges, PageInfo pageInfo) {
            this.edges = edges;
            this.pageInfo = pageInfo;
        }

        public List<VenueEdge> getEdges() {
            return edges;
        }

        public PageInfo getPageInfo() {
            return pageInfo;
        }
    }

    public static class VenueEdge {
        private final String cursor;
        private final Venue node;

        public VenueEdge(String cursor, Venue node) {
            this.cursor = cursor;
            this.node = node;
        }

        public String getCursor() {
            return cursor;
        }

        public Venue getNode() {
            return node;
        }
    }

    public static class PageInfo {
        private final boolean hasNextPage;
        private final String endCursor;

        public PageInfo(boolean hasNextPage, String endCursor) {
            this.hasNextPage = hasNextPage;
            this.endCursor = endCursor;
        }

        public boolean getHasNextPage() {
            return hasNextPage;
        }

        public String getEndCursor() {
            return endCursor;
        }
    }
}