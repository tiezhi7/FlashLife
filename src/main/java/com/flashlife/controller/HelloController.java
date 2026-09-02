package com.flashlife.controller;

import com.flashlife.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api")

public class HelloController {
    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success(
                "FlashLife backend is running!"
        );
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
