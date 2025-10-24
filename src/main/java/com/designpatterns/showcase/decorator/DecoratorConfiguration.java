package com.designpatterns.showcase.decorator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DecoratorConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "decorator.stack", havingValue = "full")
    public DataService fullyDecoratedDataService(
            @Qualifier("simpleDataService") DataService baseService,
            @Value("${decorator.caching.ttl:60000}") long ttlMillis,
            @Value("${decorator.encryption.key:default-secret-key}") String encryptionKey,
            @Value("${decorator.feature-toggle.feature-name:data-service}") String featureName,
            @Value("${decorator.feature-toggle.initially-enabled:true}") boolean initiallyEnabled) {
        
        DataService service = baseService;
        service = new LoggingDataServiceDecorator(service);
        service = new CachingDataServiceDecorator(service, ttlMillis);
        service = new EncryptionDataServiceDecorator(service, encryptionKey);
        service = new FeatureToggleDataServiceDecorator(service, featureName, initiallyEnabled);
        
        return service;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "decorator.stack", havingValue = "minimal", matchIfMissing = true)
    public DataService minimalDecoratedDataService(
            @Qualifier("simpleDataService") DataService baseService) {
        return new LoggingDataServiceDecorator(baseService);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "decorator.stack", havingValue = "caching-only")
    public DataService cachingOnlyDataService(
            @Qualifier("simpleDataService") DataService baseService,
            @Value("${decorator.caching.ttl:60000}") long ttlMillis) {
        
        DataService service = baseService;
        service = new LoggingDataServiceDecorator(service);
        service = new CachingDataServiceDecorator(service, ttlMillis);
        
        return service;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "decorator.stack", havingValue = "secure")
    public DataService secureDataService(
            @Qualifier("simpleDataService") DataService baseService,
            @Value("${decorator.encryption.key:default-secret-key}") String encryptionKey) {
        
        DataService service = baseService;
        service = new LoggingDataServiceDecorator(service);
        service = new EncryptionDataServiceDecorator(service, encryptionKey);
        
        return service;
    }
}
