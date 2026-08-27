package com.wexa.sovereignty.web;

import com.wexa.sovereignty.model.NodeContext;
import com.wexa.sovereignty.service.GraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    private final GraphService service;

    public NodeController(GraphService service) {
        this.service = service;
    }

    @GetMapping("/{id}/context")
    public NodeContext context(@PathVariable String id) {
        return service.nodeContext(id);
    }
}
