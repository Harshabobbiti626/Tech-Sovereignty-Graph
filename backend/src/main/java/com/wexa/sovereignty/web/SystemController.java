package com.wexa.sovereignty.web;

import com.wexa.sovereignty.model.HealthStatus;
import com.wexa.sovereignty.service.GraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SystemController {

    private final GraphService service;

    public SystemController(GraphService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public HealthStatus health() {
        return service.health();
    }
}
