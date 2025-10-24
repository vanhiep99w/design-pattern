package com.designpatterns.showcase.strategy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StrategyConfiguration {

    public StrategyConfiguration() {
        log.info("Strategy Pattern configuration initialized");
    }
}
