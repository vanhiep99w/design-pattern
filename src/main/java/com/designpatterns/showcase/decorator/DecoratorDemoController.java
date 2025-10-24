package com.designpatterns.showcase.decorator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/decorator")
public class DecoratorDemoController {

    private final DataService dataService;

    @Autowired
    public DecoratorDemoController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/data")
    public ResponseEntity<Map<String, String>> saveData(@RequestBody Map<String, String> request) {
        String data = request.get("data");
        String id = dataService.save(data);
        
        Map<String, String> response = new HashMap<>();
        response.put("id", id);
        response.put("message", "Data saved successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/data/{id}")
    public ResponseEntity<Map<String, String>> getData(@PathVariable String id) {
        return dataService.retrieve(id)
                .map(data -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("id", id);
                    response.put("data", data);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/data")
    public ResponseEntity<List<String>> getAllData() {
        List<String> allData = dataService.findAll();
        return ResponseEntity.ok(allData);
    }

    @DeleteMapping("/data/{id}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable String id) {
        boolean deleted = dataService.delete(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("deleted", deleted);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        dataService.clearCache();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache cleared successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        if (dataService instanceof CachingDataServiceDecorator cachingService) {
            stats.put("cacheSize", cachingService.getCacheSize());
            stats.put("cacheHits", cachingService.getCacheHits());
            stats.put("cacheMisses", cachingService.getCacheMisses());
            stats.put("cacheHitRate", cachingService.getCacheHitRate());
        } else {
            stats.put("message", "Caching decorator not active");
        }
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/feature-toggle")
    public ResponseEntity<Map<String, Object>> toggleFeature(@RequestBody Map<String, Boolean> request) {
        boolean enable = request.getOrDefault("enable", true);
        
        if (dataService instanceof FeatureToggleDataServiceDecorator featureToggleService) {
            if (enable) {
                featureToggleService.enable();
            } else {
                featureToggleService.disable();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("featureName", featureToggleService.getFeatureName());
            response.put("enabled", featureToggleService.isEnabled());
            
            return ResponseEntity.ok(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Feature toggle decorator not active");
        
        return ResponseEntity.ok(response);
    }
}
