package com.flashlife.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api")

public class HelloController {
    @GetMapping("/hello")
    public Map<String,Object> hello(){
        Map<String,Object> result = new HashMap<>();
        result.put("code",200);
        result.put(
                "message",
                "FlashLife backend is running"
        );
        return result;
    }
    @GetMapping("/info")
    public Map<String,Object> info(){
        Map<String,Object> result = new HashMap<>();
        result.put("project","FlashLife");
        result.put(
                "version",
                "1.0.0"
        );
        result.put("status","development");
        return result;
    }
    @GetMapping("/health")
    public Map<String,Object> health(){
        Map<String,Object> result = new HashMap<>();
        result.put("status","UP");
        return result;
    }
}
