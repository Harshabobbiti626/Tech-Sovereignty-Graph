package com.wexa.sovereignty.web;

import com.wexa.sovereignty.model.ImpactResult;
import com.wexa.sovereignty.service.GraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ImpactController {

    private final GraphService service;

    public ImpactController(GraphService service) {
        this.service = service;
    }

    /** Query B — blast radius of revoking a group's credentials. */
    @GetMapping("/impact/{group}")
    public ImpactResult impact(@PathVariable String group) {
        return service.impact(group);
    }
}
