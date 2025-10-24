package com.designpatterns.showcase.singleton;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SingletonConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ApplicationCacheService singletonCacheService() {
        return new ApplicationCacheService();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SessionService prototypeSessionService() {
        return new SessionService();
    }

    @Bean
    public SingletonDemoService singletonDemoService() {
        return new SingletonDemoService();
    }
}
