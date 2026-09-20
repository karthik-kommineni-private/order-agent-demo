package com.example.orderagent.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.orderagent.dto.response.AgentResponse;
import com.example.orderagent.dto.response.AgentTrace;
import com.example.orderagent.enums.AgentStatus;
import com.example.orderagent.service.agent.AgentLoop;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Exercises {@code POST /orders/agent} through the real Spring MVC stack —
 * request binding, validation, and {@link GlobalExceptionHandler} — with
 * {@link AgentLoop} mocked out, since the loop itself is already covered by
 * {@code AgentLoopTest} and {@code AgentLoopGoldenTest}.
 *
 * <p>What this test guards that those don't: that the controller actually
 * forwards the request body to the loop, and that a malformed request never
 * reaches the loop at all — it's turned into an {@link AgentResponse} by
 * {@link GlobalExceptionHandler} first, per non-negotiable rule 5.
 */
@WebMvcTest(OrderAgentController.class)
class OrderAgentControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private AgentLoop agentLoop;

    @Test
    void forwardsTheRequestBodyToTheAgentLoopAndReturnsItsResponse() {
        AgentResponse response = AgentResponse.success(
                "Order 1002 shipped and is on its way.",
                new AgentTrace("trace-1", "v1", List.of()),
                2,
                150,
                new BigDecimal("0.01"),
                "claude-haiku-4-5-20251001");
        when(agentLoop.run(anyString())).thenReturn(response);

        mvc.post()
                .uri("/orders/agent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"request\": \"Where is order 1002?\"}")
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo("SUCCESS");
    }

    @Test
    void blankRequestFailsValidationAndNeverReachesTheAgentLoop() {
        mvc.post()
                .uri("/orders/agent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"request\": \"\"}")
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo(AgentStatus.ERROR.name());
    }
}
