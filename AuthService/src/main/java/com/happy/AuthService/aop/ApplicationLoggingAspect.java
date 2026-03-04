package com.happy.AuthService.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Cross-cutting logging for controller and service methods.
 * It logs execution time and exceptions without printing sensitive method arguments.
 */
@Aspect
@Component
@Slf4j
public class ApplicationLoggingAspect {

    // Equivalent to @Slf4j annotation
    // clazz param tells the logger the source of the log messages
    // private static final Logger log = LoggerFactory.getLogger(ApplicationLoggingAspect.class);

    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Service *)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - start;
            log.info("Method {} completed in {} ms", method, elapsedMs);
            return result;
        } catch (Throwable ex) {
            long elapsedMs = System.currentTimeMillis() - start;
            log.warn("Method {} failed in {} ms: {}", method, elapsedMs, ex.getMessage());
            throw ex;
        }
    }
}
