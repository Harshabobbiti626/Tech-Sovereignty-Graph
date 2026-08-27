package com.wexa.sovereignty.web;

import com.wexa.sovereignty.model.ImpactResult;
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
@RequestMapping("/api")
public class ImpactController {

    private final GraphService service;

    public ImpactController(GraphService service) {
        this.service = service;
    }

    /** Query B — blast radius of revoking a group's credentials. */
    @GetMapping("/impact/{group}")
    public ImpactResult impact(@PathVariable @NotBlank @Size(max = 120)
                               @Pattern(regexp = "^[\\w .-]+$",
                                       message = "group may only contain letters, digits and . _ - spaces")
                               String group) {
        return service.impact(group);
    }
}
