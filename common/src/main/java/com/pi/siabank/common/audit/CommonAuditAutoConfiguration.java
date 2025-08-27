package com.pi.siabank.common.audit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@AutoConfiguration
public class CommonAuditAutoConfiguration {

    @Configuration
    @ConditionalOnClass(org.springframework.data.domain.AuditorAware.class)
    static class SpringDataAuditConfiguration {
        @Bean
        @ConditionalOnMissingBean(org.springframework.data.domain.AuditorAware.class)
        public org.springframework.data.domain.AuditorAware<String> auditorProvider() {
            return () -> {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                    return Optional.of("SYSTEM");
                }
                return Optional.ofNullable(authentication.getName());
            };
        }
    }
}
