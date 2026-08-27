package com.wexa.sovereignty.web;

import com.wexa.sovereignty.model.AuditResult;
import com.wexa.sovereignty.service.GraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuditController {

    private final GraphService service;

    public AuditController(GraphService service) {
        this.service = service;
    }

    /** Query A — how does this identity reach (a) resource(s)? */
    @GetMapping("/audit/{email}")
    public AuditResult audit(@PathVariable String email,
                             @RequestParam(required = false) String resource) {
        return service.audit(email, resource);
    }
}
