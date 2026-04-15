package com.companyapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Základní počet vláken
        executor.setMaxPoolSize(5);  // Maximální počet vláken při špičce
        executor.setQueueCapacity(100); // Kolik úkolů může čekat ve frontě
        executor.setThreadNamePrefix("AuditLogger-"); // Pojmenování vlákna v logu
        executor.initialize();
        return executor;
    }
}