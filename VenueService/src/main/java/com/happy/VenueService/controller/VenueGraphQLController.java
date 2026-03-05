package com.happy.VenueService.controller;

import java.util.Collections;
import java.util.UUID;

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

    // 构造红注入 Service
    public VenueGraphQLController(VenueService venueService) {
        this.venueService = venueService;
    }

    /**
     * @Argument: 自动将 GraphQL 请求中的参数映射为 Java 对象
     * 如果前端传了 filter: { cityCode: "SH" }，Spring 会自动填充到 VenueFilter 对象里
     */
    @QueryMapping
    public Window<Venue> venuesConnection(
            @Argument Integer first, 
            @Argument String after, 
            @Argument VenueFilter filter) {
        
        // handle cursor (ScrollPosition)
        // 如果 after 为空，说明是第一页；如果不为空，解析为基于 ID 的游标
        // if after is null, it means it is requesting the first page; if not, parse it to a cursor based on ID
        ScrollPosition position = (after != null) 
            ? ScrollPosition.forward(Collections.singletonMap("id", UUID.fromString(after)))
            : ScrollPosition.offset();
        // 调用 Service 执行带 Specification 的查询
        // 这里返回的是 Window<Venue>，Spring GraphQL 会自动将其匹配到 Schema 中的 Connection 类型
        return venueService.getVenues(filter, position, first);
    }
    @QueryMapping
    public Venue venueById(@Argument UUID id) {
        return venueService.getVenueById(id);
    }
}