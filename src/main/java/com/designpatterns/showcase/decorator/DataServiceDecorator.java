package com.designpatterns.showcase.decorator;

import java.util.List;
import java.util.Optional;

public abstract class DataServiceDecorator implements DataService {
    protected final DataService delegate;

    protected DataServiceDecorator(DataService delegate) {
        this.delegate = delegate;
    }

    @Override
    public String save(String data) {
        return delegate.save(data);
    }

    @Override
    public Optional<String> retrieve(String id) {
        return delegate.retrieve(id);
    }

    @Override
    public List<String> findAll() {
        return delegate.findAll();
    }

    @Override
    public boolean delete(String id) {
        return delegate.delete(id);
    }

    @Override
    public void clearCache() {
        delegate.clearCache();
    }
}
