package com.happy.TicketingService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DbQueryMetricsAspect {

    private final MeterRegistry registry;

    public DbQueryMetricsAspect(MeterRegistry registry) {
        this.registry = registry;
    }

    @Around("within(@org.springframework.stereotype.Repository *) || execution(* com..repository..*(..))")
    public Object aroundRepository(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getName().toLowerCase();
        String op = "other";
        if (method.startsWith("find") || method.startsWith("get") || method.startsWith("select")) op = "select";
        else if (method.startsWith("save") || method.startsWith("insert")) op = "insert";
        else if (method.startsWith("update")) op = "update";
        else if (method.startsWith("delete")) op = "delete";

        Timer.Sample sample = Timer.start(registry);
        try {
            return pjp.proceed();
        } finally {
            sample.stop(registry.timer("db.query.timer", "operation", op));
        }
    }
}
