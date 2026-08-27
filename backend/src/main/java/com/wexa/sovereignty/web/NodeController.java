package com.wexa.sovereignty.web;

import com.wexa.sovereignty.model.NodeContext;
import com.wexa.sovereignty.service.GraphService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    private final GraphService service;

    public NodeController(GraphService service) {
        this.service = service;
    }

    @GetMapping("/{id}/context")
    public NodeContext context(@PathVariable @NotBlank @Size(max = 120)
                               @Pattern(regexp = "^[\\w.-]+$",
                                       message = "node id may only contain letters, digits and . _ -")
                               String id) {
        return service.nodeContext(id);
    }
}
