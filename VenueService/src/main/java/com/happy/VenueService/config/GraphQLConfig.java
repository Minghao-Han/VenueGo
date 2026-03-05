package com.happy.VenueService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import graphql.scalars.ExtendedScalars;

@Configuration
public class GraphQLConfig {
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        // 注册扩展标量，它会自动处理 Java BigDecimal 和 GraphQL String/Number 之间的转换
        /*
        这行代码其实是一个注册动作。我们可以把它拆开来看：
        RuntimeWiringConfigurer: 这是一个接口，它的作用是“在程序运行时，告诉 GraphQL 引擎如何把 Schema 里的名称（如 scalar BigDecimal）和 Java 里的逻辑关联起来”。
        wiringBuilder: 它是 GraphQL 的“接线员”。它手里有一张表，记录了所有的类型、字段和它们的处理逻辑。
            是由 Spring 框架“传”给你的，而不是由你定义的。
        .scalar(...): 这是在告诉接线员：“嘿，我要注册一个新的原子类型（Scalar）”。
        ExtendedScalars.GraphQLBigDecimal: 这是预定义好的“处理逻辑包裹”。它包含了：
            解析逻辑：怎么把前端传来的 199.99 变成 Java 的 new BigDecimal("199.99")。
            序列化逻辑：怎么把数据库里的 BigDecimal 变成前端看到的数字。
            校验逻辑：如果前端传了一个 "abc" 进来，它会直接报错，不让非法数据进入你的 Service 层。
        */
        return wiringBuilder -> 
            wiringBuilder.scalar(ExtendedScalars.GraphQLBigDecimal)
            .scalar(ExtendedScalars.DateTime);
    }
}