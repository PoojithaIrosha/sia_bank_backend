package com.pi.siabank.authservice;

import com.pi.siabank.common.audit.CommonAuditAutoConfiguration;
import com.pi.siabank.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableJpaAuditing
@Import({GlobalExceptionHandler.class, CommonAuditAutoConfiguration.class})
@EnableMethodSecurity
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
