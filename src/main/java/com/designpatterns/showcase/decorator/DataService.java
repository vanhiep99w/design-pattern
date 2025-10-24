package com.designpatterns.showcase.decorator;

import java.util.List;
import java.util.Optional;

public interface DataService {
    String save(String data);
    Optional<String> retrieve(String id);
    List<String> findAll();
    boolean delete(String id);
    void clearCache();
}
