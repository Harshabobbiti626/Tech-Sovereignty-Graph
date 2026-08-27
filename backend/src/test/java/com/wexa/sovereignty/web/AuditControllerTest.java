package com.wexa.sovereignty.web;

import com.wexa.sovereignty.core.CircuitBreaker;
import com.wexa.sovereignty.core.GraphExecutor;
import com.wexa.sovereignty.model.AuditPath;
import com.wexa.sovereignty.model.AuditResult;
import com.wexa.sovereignty.model.IdentitySummary;
import com.wexa.sovereignty.service.GraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GraphService service;

    @MockBean
    private GraphExecutor executor; // the advice needs it for the breaker countdown

    @Test
    void returnsAuditPayload() throws Exception {
        when(service.audit("former_vendor_consultant@external.com", null)).thenReturn(new AuditResult(
                new IdentitySummary("former_vendor_consultant@external.com", "Vendor Consultant", "Suspended"),
                List.of(new AuditPath(4, true, "res_pii", "Customer_PII_Database", "Critical", List.of())),
                1));

        mvc.perform(get("/api/audit/former_vendor_consultant@external.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identity.email").value("former_vendor_consultant@external.com"))
                .andExpect(jsonPath("$.toxicCount").value(1));
    }

    @Test
    void rejectsMalformedIdentityWith400() throws Exception {
        mvc.perform(get("/api/audit/not%20a%20valid%20identity!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void mapsDatabaseOutageTo503WithRetryWindow() throws Exception {
        when(executor.breaker()).thenReturn(new CircuitBreaker(3, 15000));
        when(service.audit("former_vendor_consultant@external.com", null))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "db down"));

        mvc.perform(get("/api/audit/former_vendor_consultant@external.com"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.retryInMs").isNumber());
    }
}
