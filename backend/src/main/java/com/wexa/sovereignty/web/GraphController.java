package com.wexa.sovereignty.web;

import com.wexa.sovereignty.model.GraphPayload;
import com.wexa.sovereignty.model.GraphStats;
import com.wexa.sovereignty.service.GraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GraphController {

    private final GraphService service;

    public GraphController(GraphService service) {
        this.service = service;
    }

    @GetMapping("/graph")
    public GraphPayload graph() {
        return service.graph();
    }

    @GetMapping("/stats")
    public GraphStats stats() {
        return service.stats();
    }
}
