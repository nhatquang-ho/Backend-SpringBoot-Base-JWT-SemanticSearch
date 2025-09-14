package com.example.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class SslConfig {

    // This class can be extended for custom SSL configurations
    // For now, SSL configuration is handled through application.yml

    // Example: Custom SSL context configuration
    // @Bean
    // public SSLContext sslContext() throws Exception {
    //     // Custom SSL context implementation
    //     return SSLContext.getDefault();
    // }
}