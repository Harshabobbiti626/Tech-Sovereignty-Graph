package com.wexa.sovereignty.web;

import com.wexa.sovereignty.model.AuditResult;
import com.wexa.sovereignty.service.GraphService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class AuditController {

    private final GraphService service;

    public AuditController(GraphService service) {
        this.service = service;
    }

    /**
     * Query A — how does this identity reach (a) resource(s)?
     * The pattern admits both email addresses and agent slugs like production_ci_cd_bot.
     */
    @GetMapping("/audit/{email}")
    public AuditResult audit(@PathVariable @NotBlank @Size(max = 120)
                             @Pattern(regexp = "^[\\w.@+-]+$",
                                     message = "identity may only contain letters, digits and . _ + - @")
                             String email,
                             @RequestParam(required = false) @Size(max = 120) String resource) {
        return service.audit(email, resource);
    }
}
