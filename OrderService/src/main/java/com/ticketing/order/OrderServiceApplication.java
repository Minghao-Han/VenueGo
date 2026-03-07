package com.ticketing.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;

/**
 * Order Service Application Entry Point
 */
@SpringBootApplication
@EnableConfigurationProperties
@MapperScan("com.ticketing.order.infrastructure.repository.mapper")
@Import(MybatisPlusAutoConfiguration.class)
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
