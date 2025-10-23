package com.designpatterns.showcase.dependencyinjection.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OnboardingProperties.class)
public class DependencyInjectionConfiguration {
}
