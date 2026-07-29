package com.streamverse.video.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(FfmpegProperties.class)
public class TranscodingConfig {

    /**
     * Dedicated pool for transcoding. Transcoding is CPU-bound, so keep the
     * pool small; extra jobs queue instead of thrashing the CPU.
     */
    @Bean(name = "transcodeExecutor")
    public Executor transcodeExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("transcode-");
        executor.initialize();
        return executor;
    }
}
