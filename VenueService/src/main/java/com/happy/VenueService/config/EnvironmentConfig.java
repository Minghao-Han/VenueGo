package com.happy.VenueService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {

    private final String env;

    // Default to "dev" if ENV variable is not set
    public EnvironmentConfig(@Value("${ENV:dev}") String env) {
        this.env = env;
    }

    public boolean isProd() {
        return "prod".equalsIgnoreCase(env);
    }

    public boolean isDev() {
        return "dev".equalsIgnoreCase(env);
    }

    public String getEnv() {
        return env;
    }
}
