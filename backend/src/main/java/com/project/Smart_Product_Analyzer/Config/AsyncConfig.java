package com.project.Smart_Product_Analyzer.Config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core pool size: Minimum threads to keep alive.
        // 5 threads = 5 parallel browsers/AI requests.
        executor.setCorePoolSize(5);

        // Max pool size: Maximum threads under load
        executor.setMaxPoolSize(10);

        // Queue capacity: tasks waiting if pool is full
        executor.setQueueCapacity(25);

        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            System.err.println("Exception in Async Method: " + method.getName());
            ex.printStackTrace();
        };
    }
}
