package com.streamverse.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * Supplies the current actor id for {@code created_by} / {@code updated_by}.
     * There is no auth layer in this standalone module yet, so this returns
     * empty. Wire it to your SecurityContext principal when auth is added.
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return Optional::empty;
    }
}
