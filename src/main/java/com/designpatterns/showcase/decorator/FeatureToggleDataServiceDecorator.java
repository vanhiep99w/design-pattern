package com.designpatterns.showcase.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class FeatureToggleDataServiceDecorator extends DataServiceDecorator {
    private static final Logger logger = LoggerFactory.getLogger(FeatureToggleDataServiceDecorator.class);
    private boolean enabled;
    private final String featureName;

    public FeatureToggleDataServiceDecorator(DataService delegate, String featureName, boolean enabled) {
        super(delegate);
        this.featureName = featureName;
        this.enabled = enabled;
    }

    @Override
    public String save(String data) {
        if (!enabled) {
            logger.warn("Feature '{}' is disabled. Save operation blocked.", featureName);
            throw new FeatureDisabledException("Feature '" + featureName + "' is currently disabled");
        }
        return delegate.save(data);
    }

    @Override
    public Optional<String> retrieve(String id) {
        if (!enabled) {
            logger.warn("Feature '{}' is disabled. Retrieve operation blocked.", featureName);
            return Optional.empty();
        }
        return delegate.retrieve(id);
    }

    @Override
    public List<String> findAll() {
        if (!enabled) {
            logger.warn("Feature '{}' is disabled. FindAll operation blocked.", featureName);
            return List.of();
        }
        return delegate.findAll();
    }

    @Override
    public boolean delete(String id) {
        if (!enabled) {
            logger.warn("Feature '{}' is disabled. Delete operation blocked.", featureName);
            return false;
        }
        return delegate.delete(id);
    }

    @Override
    public void clearCache() {
        if (!enabled) {
            logger.warn("Feature '{}' is disabled. ClearCache operation blocked.", featureName);
            return;
        }
        delegate.clearCache();
    }

    public void enable() {
        logger.info("Enabling feature '{}'", featureName);
        this.enabled = true;
    }

    public void disable() {
        logger.info("Disabling feature '{}'", featureName);
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFeatureName() {
        return featureName;
    }
}
